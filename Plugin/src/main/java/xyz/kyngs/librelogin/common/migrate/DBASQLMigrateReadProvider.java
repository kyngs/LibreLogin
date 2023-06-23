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

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class DBASQLMigrateReadProvider extends SQLMigrateReadProvider {

    public DBASQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
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
                            new AuthenticUser(
                                    uuid,
                                    rs.getBoolean("premium") ? uuid : null,
                                    password,
                                    name,
                                    rs.getTimestamp("firstjoin"),
                                    rs.getTimestamp("lastjoin"),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            )
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
