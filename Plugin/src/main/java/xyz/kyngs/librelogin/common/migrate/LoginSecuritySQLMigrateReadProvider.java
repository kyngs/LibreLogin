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

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class LoginSecuritySQLMigrateReadProvider extends SQLMigrateReadProvider {
    public LoginSecuritySQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                var uniqueId = UUID.fromString(rs.getString("unique_user_id"));
                var lastNickname = rs.getString("last_name");
                var lastSeen = rs.getTimestamp("last_login");
                var firstSeen = rs.getTimestamp("registration_date");
                var rawPassword = rs.getString("password");
                var hashingAlgorithm = rs.getInt("hashing_algorithm");

                HashedPassword hashed;

                if (hashingAlgorithm == 7) {
                    hashed = CryptoUtil.convertFromBCryptRaw(rawPassword);
                } else {
                    logger.warn("User %s has invalid algorithm %s, omitting".formatted(lastNickname, hashingAlgorithm));
                    continue;
                }

                users.add(new AuthenticUser(
                        uniqueId,
                        null,
                        hashed,
                        lastNickname,
                        firstSeen,
                        lastSeen,
                        null,
                        null,
                        lastSeen,
                        null,
                        null
                ));
            }

            return users;
        });
    }
}
