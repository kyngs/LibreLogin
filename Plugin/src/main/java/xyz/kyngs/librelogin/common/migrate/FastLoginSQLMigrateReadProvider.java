/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.migrate;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.database.connector.SQLDatabaseConnector;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.util.Collection;
import java.util.List;

public class FastLoginSQLMigrateReadProvider extends SQLMigrateReadProvider {
    private final SQLDatabaseConnector main;

    public FastLoginSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector, SQLDatabaseConnector main) {
        super(tableName, logger, connector);
        this.main = main;
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    if (rs.getInt("Premium") != 1) continue;

                    var premiumUUID = GeneralUtil.fromUnDashedUUID(rs.getString("UUID"));
                    var name = rs.getString("Name");

                    main.runQuery(connection2 -> {
                        var ps2 = connection2.prepareStatement("UPDATE librepremium_data SET premium_uuid=? WHERE last_nickname=?");
                        ps2.setString(1, premiumUUID.toString());
                        ps2.setString(2, name);
                        ps2.executeUpdate();
                    });
                } catch (Exception e) {
                    logger.error("Error while migrating user from FastLogin db, omitting");
                }
            }

            return List.of();
        });
    }
}
