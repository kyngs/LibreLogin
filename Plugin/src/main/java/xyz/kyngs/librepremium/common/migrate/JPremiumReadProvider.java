package xyz.kyngs.librepremium.common.migrate;

import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class JPremiumReadProvider extends MySQLReadProvider {

    public JPremiumReadProvider(EasyDB<MySQL, Connection, SQLException> easyDB, String tableName, Logger logger) {
        super(easyDB, tableName, logger);
    }

    @Override
    public User getByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> getAllUsers() {
        return easyDB.runFunctionSync(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM %s".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var uniqueIdString = rs.getString("uniqueId");
                    var premiumIdString = rs.getString("premiumId");
                    var lastNickname = rs.getString("lastNickname");
                    var lastSeen = rs.getTimestamp("lastSeen");
                    var firstSeen = rs.getTimestamp("firstSeen");
                    var rawPassword = rs.getString("hashedPassword");

                    if (lastNickname == null) continue; //Yes this may happen

                    HashedPassword password = null;

                    if (rawPassword != null) {
                        var split = rawPassword.split("\\$");

                        var algo = split[0];
                        var salt = split[1];
                        var hash = split[2];

                        algo = switch (algo) {
                            case "SHA256" -> "SHA-256";
                            default -> {
                                logger.error("User %s has invalid algorithm %s, omitting".formatted(lastNickname, algo));
                                yield null;
                            }
                        };

                        if (algo == null) continue;

                        password = new HashedPassword(
                                hash,
                                salt,
                                algo
                        );

                    }

                    users.add(new User(
                            GeneralUtil.fromUnDashedUUID(uniqueIdString),
                            premiumIdString == null ? null : GeneralUtil.fromUnDashedUUID(premiumIdString),
                            password,
                            lastNickname,
                            firstSeen,
                            lastSeen
                    ));

                } catch (Exception e) {
                    logger.error("Failed to read user from JPremium db, omitting");
                }
            }

            return users;

        });
    }
}
