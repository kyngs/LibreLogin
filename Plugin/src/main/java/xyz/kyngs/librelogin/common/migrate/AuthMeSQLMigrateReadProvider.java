package xyz.kyngs.librelogin.common.migrate;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.database.connector.SQLDatabaseConnector;
import xyz.kyngs.librelogin.common.database.AuthenticUser;
import xyz.kyngs.librelogin.common.util.CryptoUtil;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;

public class AuthMeSQLMigrateReadProvider extends SQLMigrateReadProvider {

    public AuthMeSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var nickname = rs.getString("realname");
                    var passwordRaw = rs.getString("password");
                    var lastSeen = rs.getLong("lastlogin");
                    var firstSeen = rs.getLong("regdate");

                    if (nickname == null) continue;

                    HashedPassword password = null;

                    if (passwordRaw != null) {
                        if (passwordRaw.startsWith("$SHA$")) {
                            var split = passwordRaw.split("\\$");

                            var algo = "SHA-256";
                            var salt = split[2];
                            var hash = split[3];

                            password = new HashedPassword(hash, salt, algo);
                        } else if (passwordRaw.startsWith("$2a$")) {
                            password = CryptoUtil.convertFromBCryptRaw(passwordRaw);
                        } else {
                            logger.error("User " + nickname + " has an invalid password hash");
                        }
                    }

                    users.add(
                            new AuthenticUser(
                                    GeneralUtil.getCrackedUUIDFromName(nickname),
                                    null,
                                    password,
                                    nickname,
                                    firstSeen == 0 ? null : new Timestamp(firstSeen),
                                    lastSeen == 0 ? null : new Timestamp(lastSeen),
                                    null,
                                    null,
                                    null,
                                    null)
                    );

                } catch (Exception e) {
                    logger.error("Failed to read user from AuthMe db, omitting");
                }
            }

            return users;
        });
    }
}
