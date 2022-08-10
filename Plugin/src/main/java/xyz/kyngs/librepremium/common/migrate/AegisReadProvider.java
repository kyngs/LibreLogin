package xyz.kyngs.librepremium.common.migrate;

import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.util.CryptoUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class AegisReadProvider extends MySQLReadProvider {

    public AegisReadProvider(EasyDB<MySQL, Connection, SQLException> easyDB, String tableName, Logger logger) {
        super(easyDB, tableName, logger);
    }

    @Override
    public User getByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> getAllUsers() {
        return easyDB.runFunctionSync(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {

                try {
                    var uuid = UUID.fromString(rs.getString("uuid").replace(".", "")); //Aegis at it again, this time with a dot.
                    var onlineID = rs.getString("onlineId");
                    var nickname = rs.getString("name");
                    var passwordRaw = rs.getString("password");

                    HashedPassword password = null;

                    if (passwordRaw != null && !passwordRaw.contentEquals("")) { //God-damn Aegis.
                        if (passwordRaw.startsWith("$2a$")) {
                            password = CryptoUtil.convertFromBCryptRaw(passwordRaw);
                        } else {
                            logger.error("User " + nickname + " has an invalid password hash");
                        }
                    }

                    users.add(new User(
                            uuid,
                            onlineID == null || !rs.getBoolean("premium") ? null : UUID.fromString(onlineID.replace(".", "")), //Aegis at it again, this time with a dot.
                            password,
                            nickname,
                            Timestamp.valueOf(LocalDateTime.now()),
                            Timestamp.valueOf(LocalDateTime.now()),
                            null,
                            null,
                            null,
                            null));

                } catch (Exception e) {
                    logger.error("Failed to read user from Aegis db, omitting");
                }

            }

            return users;
        });
    }
}
