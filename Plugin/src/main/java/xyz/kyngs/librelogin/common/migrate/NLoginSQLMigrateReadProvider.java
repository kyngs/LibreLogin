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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

public class NLoginSQLMigrateReadProvider extends SQLMigrateReadProvider {
    public NLoginSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
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
                    var uniqueIdString = rs.getString("uniqueId");
                    var premiumIdString = rs.getString("premiumId");
                    var lastNickname = rs.getString("realname");
                    var lastSeen = rs.getTimestamp("lastlogin");
                    var firstSeen = rs.getTimestamp("regdate");
                    var rawPassword = rs.getString("password");
                    var ip = rs.getString("address");

                    if (lastNickname == null) continue; //Yes this may happen

                    HashedPassword password = null;

                    if (rawPassword != null) {
                        if (!rawPassword.startsWith("$SHA512$")) {
                            logger.error("User %s has invalid algorithm %s, omitting".formatted(lastNickname, rawPassword));
                            continue;
                        }
                        var split = rawPassword.substring(8).split("\\$");
                        password = new HashedPassword(
                                split[0],
                                split[1],
                                "SHA-512"
                        );
                    }

                    users.add(new AuthenticUser(
                            GeneralUtil.fromUnDashedUUID(uniqueIdString),
                            premiumIdString == null ? null : GeneralUtil.fromUnDashedUUID(premiumIdString),
                            password,
                            lastNickname,
                            firstSeen,
                            lastSeen,
                            null,
                            ip,
                            Timestamp.from(Instant.EPOCH),
                            null,
                            null
                    ));

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error while reading user from database");
                }
            }

            return users;
        });
    }
}
