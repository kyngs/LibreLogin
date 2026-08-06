/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.migrate;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.database.connector.SQLDatabaseConnector;
import xyz.kyngs.librelogin.common.database.AuthenticUser;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class LogItSQLMigrateReadProvider extends SQLMigrateReadProvider {

    public LogItSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM %s".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var nickname = rs.getString("username");
                    var password = rs.getString("password");
                    var salt = rs.getString("salt");
                    var algorithm = rs.getString("hashing_algorithm");
                    var lastSeen = rs.getLong("last_active_date");
                    var firstSeen = rs.getLong("reg_date");
                    var ip = rs.getString("ip");
                    var uuid = rs.getString("uuid");
                    var email = rs.getString("email");

                    if (email != null && email.isBlank()) email = null;

                    if (nickname == null) continue;

                    HashedPassword parsedPassword = null;

                    if (algorithm.equals("sha-256")) {
                        parsedPassword = new HashedPassword(password, salt, "LOGIT-SHA-256");
                    } else {
                        logger.warn("Unsupported hashing algorithm: " + algorithm + ", skipping user " + nickname);
                    }

                    users.add(
                            new AuthenticUser(
                                    UUID.fromString(uuid),
                                    null,
                                    parsedPassword,
                                    nickname,
                                    firstSeen == 0 ? null : new Timestamp(firstSeen),
                                    lastSeen == 0 ? null : new Timestamp(lastSeen),
                                    null,
                                    ip,
                                    null,
                                    null,
                                    email
                            )
                    );

                } catch (Exception e) {
                    logger.error("Failed to read user from LogIt db, omitting");
                }
            }

            return users;
        });
    }
}
