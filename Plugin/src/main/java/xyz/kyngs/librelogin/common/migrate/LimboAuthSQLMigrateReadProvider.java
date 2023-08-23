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
import xyz.kyngs.librelogin.common.util.CryptoUtil;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class LimboAuthSQLMigrateReadProvider extends SQLMigrateReadProvider {

    public LimboAuthSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var uniqueIdString = rs.getString("UUID");
                    var premiumIdString = rs.getString("PREMIUMUUID");
                    var lastNickname = rs.getString("NICKNAME");
                    var lastSeen = rs.getLong("LOGINDATE");
                    var firstSeen = rs.getLong("REGDATE");
                    var rawPassword = rs.getString("HASH");
                    var ip = rs.getString("IP");

                    if (lastNickname == null) continue; //Yes this may happen

                    if (premiumIdString.isEmpty()) {
                        premiumIdString = null;
                    }

                    HashedPassword password = null;

                    if (rawPassword != null) {
                        if (rawPassword.startsWith("SHA256$")) {
                            var split = rawPassword.split("\\$");

                            var algo = "SHA-256";
                            var salt = split[1];
                            var hash = split[2];
                            password = new HashedPassword(hash, salt, algo);
                        } else if (rawPassword.startsWith("$2a$")) {
                            password = CryptoUtil.convertFromBCryptRaw(rawPassword);
                        } else {
                            logger.error("User " + lastNickname + " has an invalid password hash");
                        }
                    }

                    users.add(new AuthenticUser(
                            UUID.fromString(uniqueIdString),
                            premiumIdString == null ? null : UUID.fromString(premiumIdString),
                            password,
                            lastNickname,
                            new Timestamp(firstSeen),
                            new Timestamp(lastSeen),
                            null,
                            ip,
                            null,
                            null,
                            null
                    ));

                } catch (Exception e) {
                    logger.error("Failed to read user from LimboAuth db, omitting. Error: " + e.getMessage());
                }
            }

            return users;

        });
    }
}
