package xyz.kyngs.librelogin.common.migrate;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.database.connector.SQLDatabaseConnector;
import xyz.kyngs.librelogin.common.database.AuthenticUser;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class AuthySQLMigrateReadProvider extends SQLMigrateReadProvider {
    public AuthySQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
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
                    var uuid = rs.getString("uuid");
                    var username = rs.getString("username");
                    var ip = rs.getString("ip");
                    var passwordHash = rs.getString("password");

                    if (uuid == null || username == null) continue;

                    var password = new HashedPassword(passwordHash, null, "SHA-256");

                    users.add(new AuthenticUser(
                            UUID.fromString(uuid),
                            null,
                            password,
                            username,
                            Timestamp.from(Instant.now()),
                            Timestamp.from(Instant.now()),
                            null,
                            ip,
                            null,
                            null,
                            null
                    ));
                } catch (Exception e) {
                    logger.error("Error while migrating user from Authy db, omitting");
                }
            }

            return users;
        });
    }
}
