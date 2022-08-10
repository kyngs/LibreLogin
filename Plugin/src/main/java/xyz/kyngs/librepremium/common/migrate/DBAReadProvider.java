package xyz.kyngs.librepremium.common.migrate;

import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class DBAReadProvider extends MySQLReadProvider {
    public DBAReadProvider(EasyDB<MySQL, Connection, SQLException> easyDB, String tableName, Logger logger) {
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
                    var uuid = UUID.fromString(rs.getString("uuid"));
                    var name = rs.getString("name");

                    var hash = rs.getString("password");
                    var salt = rs.getString("salt");

                    HashedPassword password;

                    if (hash != null && salt != null) {
                        password = new HashedPassword(
                                hash,
                                salt,
                                "SHA-512"
                        );
                    } else password = null;

                    users.add(
                            new User(
                                    uuid,
                                    rs.getBoolean("premium") ? uuid : null,
                                    password,
                                    name,
                                    rs.getTimestamp("firstjoin"),
                                    rs.getTimestamp("lastjoin"),
                                    null,
                                    null,
                                    null,
                                    null)
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Failed to read user from DBA db, omitting");
                }
            }

            return users;
        });
    }
}
