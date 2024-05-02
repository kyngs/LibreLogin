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
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.util.Collection;
import java.util.HashSet;

public class CrazyLoginSQLMigrateReadProvider extends SQLMigrateReadProvider {

    public CrazyLoginSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
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
                    var nickname = rs.getString("name");
                    var passwordRaw = rs.getString("password");
                    var lastSeen = rs.getTimestamp("lastAction");

                    if (nickname == null) continue;

                    HashedPassword password = null;

                    if (passwordRaw != null) {
                        if (passwordRaw.startsWith("$SHA$")) {
                            var split = passwordRaw.split("\\$");

                            var algo = "SHA-256";
                            var salt = split[2];
                            var hash = split[3];

                            password = new HashedPassword(hash, salt, algo);
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
                                    null,
                                    lastSeen,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            )
                    );

                } catch (Exception e) {
                    logger.error("Failed to read user from CrazyLogin db, omitting");
                }
            }

            return users;
        });
    }
}
