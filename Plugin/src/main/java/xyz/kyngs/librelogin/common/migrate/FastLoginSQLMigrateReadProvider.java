/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.migrate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.database.connector.DatabaseConnector;
import xyz.kyngs.librelogin.api.database.connector.SQLDatabaseConnector;
import xyz.kyngs.librelogin.api.premium.PremiumException;
import xyz.kyngs.librelogin.api.premium.PremiumProvider;
import xyz.kyngs.librelogin.api.premium.PremiumUser;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.util.*;

public class FastLoginSQLMigrateReadProvider extends SQLMigrateReadProvider {
    private final DatabaseConnector<?, ?> main;
    private final PremiumProvider provider;

    public FastLoginSQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector, DatabaseConnector<?, ?> main, PremiumProvider provider) {
        super(tableName, logger, connector);
        this.main = main;
        this.provider = provider;
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            Multimap<UUID, String> premiumUsers = HashMultimap.create();

            while (rs.next()) {
                try {
                    if (rs.getInt("Premium") != 1) continue;

                    var premiumUUID = GeneralUtil.fromUnDashedUUID(rs.getString("UUID"));
                    var name = rs.getString("Name");

                    premiumUsers.put(premiumUUID, name);
                } catch (Exception e) {
                    logger.error("Error while migrating user from FastLogin db, omitting");
                }
            }

            for (Map.Entry<UUID, Collection<String>> entry : premiumUsers.asMap().entrySet()) {
                var names = entry.getValue();
                var premiumUUID = entry.getKey();

                if (premiumUUID == null) {
                    continue;
                }

                String name = null;

                if (names.size() == 1) {
                    name = names.iterator().next();
                } else if (names.size() > 1) {
                    logger.warn("Users %s share the same premium UUID %s, contacting mojang to find the owner".formatted(Arrays.toString(names.toArray()), premiumUUID));
                    PremiumUser user = null;

                    while (true) {
                        try {
                            user = provider.getUserForUUID(premiumUUID);
                            break;
                        } catch (PremiumException e) {
                            if (e.getIssue() == PremiumException.Issue.THROTTLED) {
                                logger.warn("Request to mojang throttled, waiting for 5 seconds");
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException interruptedException) {
                                    throw new RuntimeException(interruptedException); //Probably should not continue when interrupted
                                }
                            } else {
                                logger.error("Cannot contact mojang to find the owner, omitting");
                                e.printStackTrace();
                                break;
                            }
                        }
                    }

                    if (user == null) {
                        logger.warn("No owner found for the premium UUID %s, omitting".formatted(premiumUUID));
                        continue;
                    }

                    for (String s : names) {
                        if (s.equalsIgnoreCase(user.name())) {
                            name = s;
                            break;
                        }
                    }

                    if (name == null) {
                        logger.error("Registered names with the premium UUID do not match the mojang name %s, omitting".formatted(user.name()));
                        continue;
                    } else {
                        logger.info("Found owner of the premium UUID %s, name %s".formatted(premiumUUID, name));
                    }
                } else {
                    continue;
                }

                assert name != null;

                if (main instanceof SQLDatabaseConnector sqlMain) {
                    String finalName = name;
                    sqlMain.runQuery(connection2 -> {
                        var ps2 = connection2.prepareStatement("UPDATE librepremium_data SET premium_uuid=? WHERE last_nickname=?");
                        ps2.setString(1, premiumUUID.toString());
                        ps2.setString(2, finalName);
                        ps2.executeUpdate();
                    });
                }
            }

            return List.of();
        });
    }
}
