package xyz.kyngs.librepremium.common.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.EasyDBConfig;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.easydb.provider.mysql.MySQLConfig;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.ReadWriteDatabaseProvider;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.util.MultipleSetter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MySQLDatabaseProvider implements ReadWriteDatabaseProvider {

    private final EasyDB<MySQL, Connection, SQLException> easyDB;
    private final Cache<UUID, User> userCache;
    private final Logger logger;

    public MySQLDatabaseProvider(PluginConfiguration configuration, Logger logger) {
        this.logger = logger;

        userCache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();

        easyDB = new EasyDB<>(
                new EasyDBConfig<>(
                        new MySQL(
                                new MySQLConfig()
                                        .setUsername(configuration.getDatabaseUsername())
                                        .setPassword(configuration.getDatabasePassword())
                                        .setJdbcUrl("jdbc:mysql://%s:%s/%s?autoReconnect=true".formatted(configuration.getHost(), configuration.getPort(), configuration.getDatabase()))
                        )
                )
                        .setExceptionHandler(this::handleException)
                        .setConnectionExceptionHandler(this::handleConnectionException)
                        .useGlobalExecutor(true)
        );

        validateTables();
    }

    private void validateTables() {
        easyDB.runTaskSync(connection -> connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS librepremium_data(" +
                        "uuid VARCHAR(256) NOT NULL PRIMARY KEY," +
                        "premium_uuid VARCHAR(256)," +
                        "hashed_password VARCHAR(256)," +
                        "salt VARCHAR(256)," +
                        "algo VARCHAR(256)," +
                        "last_nickname VARCHAR(256) NOT NULL," +
                        "joined TIMESTAMP NOT NULL," +
                        "last_seen TIMESTAMP NOT NULL" +
                        ")"
        ).executeUpdate());
    }

    private boolean handleConnectionException(Exception e) {
        logger.error("!! LOST CONNECTION TO THE DATABASE, THE PROXY IS GOING TO SHUT DOWN TO PREVENT DAMAGE !!");
        e.printStackTrace();
        System.exit(1);
        //Won't return anyway
        return true;
    }

    private boolean handleException(Exception e) {
        e.printStackTrace();
        return true;
    }

    @Override
    public User getByName(String name) {
        return easyDB.runFunctionSync(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE last_nickname=?");

            ps.setString(1, name);

            var rs = ps.executeQuery();

            if (rs.next()) {
                var id = UUID.fromString(rs.getString("uuid"));
                var premiumUUID = rs.getString("premium_uuid");
                var hashedPassword = rs.getString("hashed_password");
                var salt = rs.getString("salt");
                var algo = rs.getString("algo");
                var lastNickname = rs.getString("last_nickname");
                var joinDate = rs.getTimestamp("joined");
                var lastSeen = rs.getTimestamp("last_seen");

                return userCache.get(id, x -> new User(
                        id,
                        premiumUUID == null ? null : UUID.fromString(premiumUUID),
                        hashedPassword == null ? null : new HashedPassword(
                                hashedPassword,
                                salt,
                                algo
                        ),
                        lastNickname,
                        joinDate,
                        lastSeen
                ));
            } else return null;

        });
    }

    @Override
    public User getByUUID(UUID uuid) {
        return userCache.get(uuid, x -> easyDB.runFunctionSync(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE uuid=?");

            ps.setString(1, uuid.toString());

            var rs = ps.executeQuery();

            if (rs.next()) {
                var id = UUID.fromString(rs.getString("uuid"));
                var premiumUUID = rs.getString("premium_uuid");
                var hashedPassword = rs.getString("hashed_password");
                var salt = rs.getString("salt");
                var algo = rs.getString("algo");
                var lastNickname = rs.getString("last_nickname");
                var joinDate = rs.getTimestamp("joined");
                var lastSeen = rs.getTimestamp("last_seen");

                return new User(
                        id,
                        premiumUUID == null ? null : UUID.fromString(premiumUUID),
                        hashedPassword == null ? null : new HashedPassword(
                                hashedPassword,
                                salt,
                                algo
                        ),
                        lastNickname,
                        joinDate,
                        lastSeen
                );
            } else return null;

        }));
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        return easyDB.runFunctionSync(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE premium_uuid=?");

            ps.setString(1, uuid.toString());

            var rs = ps.executeQuery();

            if (rs.next()) {
                var id = UUID.fromString(rs.getString("uuid"));
                var premiumUUID = rs.getString("premium_uuid");
                var hashedPassword = rs.getString("hashed_password");
                var salt = rs.getString("salt");
                var algo = rs.getString("algo");
                var lastNickname = rs.getString("last_nickname");
                var joinDate = rs.getTimestamp("joined");
                var lastSeen = rs.getTimestamp("last_seen");

                return userCache.get(id, x -> new User(
                        id,
                        premiumUUID == null ? null : UUID.fromString(premiumUUID),
                        hashedPassword == null ? null : new HashedPassword(
                                hashedPassword,
                                salt,
                                algo
                        ),
                        lastNickname,
                        joinDate,
                        lastSeen
                ));
            } else return null;
        });
    }

    @Override
    public Collection<User> getAllUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveUser(User user) {
        easyDB.runTaskSync(connection -> {
            var ps = connection.prepareStatement("INSERT INTO librepremium_data(uuid, premium_uuid, hashed_password, salt, algo, last_nickname, joined, last_seen) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=?, premium_uuid=?, hashed_password=?, salt=?, algo=?, last_nickname=?, joined=?, last_seen=?");

            var password = user.getHashedPassword();

            var setter = new MultipleSetter();

            setter.set(1, user.getUuid().toString());
            setter.set(2, user.getPremiumUUID() == null ? null : user.getPremiumUUID().toString());
            setter.set(3, password == null ? null : password.hash());
            setter.set(4, password == null ? null : password.salt());
            setter.set(5, password == null ? null : password.algo());
            setter.set(6, user.getLastNickname());
            setter.set(7, user.getJoinDate());
            setter.set(8, user.getLastSeen());

            setter.apply(ps, 2);

            ps.executeUpdate();
        });
    }

    @Override
    public void saveUsers(Collection<User> users) {
        easyDB.runTaskSync(connection -> {
            var ps = connection.prepareStatement("INSERT INTO librepremium_data(uuid, premium_uuid, hashed_password, salt, algo, last_nickname, joined, last_seen) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=?, premium_uuid=?, hashed_password=?, salt=?, algo=?, last_nickname=?, joined=?, last_seen=?");

            for (User user : users) {
                try {
                    var setter = new MultipleSetter();
                    var password = user.getHashedPassword();

                    setter.set(1, user.getUuid().toString());
                    setter.set(2, user.getPremiumUUID() == null ? null : user.getPremiumUUID().toString());
                    setter.set(3, password == null ? null : password.hash());
                    setter.set(4, password == null ? null : password.salt());
                    setter.set(5, password == null ? null : password.algo());
                    setter.set(6, user.getLastNickname());
                    setter.set(7, user.getJoinDate());
                    setter.set(8, user.getLastSeen());

                    setter.apply(ps, 2);

                    ps.addBatch();
                } catch (Exception e) {
                    logger.error("Failed to save user %s, omitting".formatted(user.getUuid()));
                    e.printStackTrace();
                }

            }

            ps.executeBatch();
        });
    }

    public void disable() {
        easyDB.stop();
    }
}
