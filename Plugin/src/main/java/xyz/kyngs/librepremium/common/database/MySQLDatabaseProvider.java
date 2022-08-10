package xyz.kyngs.librepremium.common.database;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.EasyDBConfig;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.easydb.provider.mysql.MySQLConfig;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.ReadWriteDatabaseProvider;
import xyz.kyngs.librepremium.api.database.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MySQLDatabaseProvider implements ReadWriteDatabaseProvider {

    private final EasyDB<MySQL, Connection, SQLException> easyDB;
    private final Logger logger;

    public MySQLDatabaseProvider(PluginConfiguration configuration, Logger logger) {
        this.logger = logger;

        var mySQLConfig = new MySQLConfig()
                .setUsername(configuration.getDatabaseUser())
                .setPassword(configuration.getDatabasePassword())
                .setJdbcUrl("jdbc:mysql://%s:%s/%s?autoReconnect=true&zeroDateTimeBehavior=convertToNull".formatted(configuration.getDatabaseHost(), configuration.getDatabasePort(), configuration.getDatabaseName()));

        mySQLConfig.getHikariConfig().setMaxLifetime(configuration.maxLifeTime());
        easyDB = new EasyDB<>(
                new EasyDBConfig<>(
                        new MySQL(
                                mySQLConfig
                        )
                )
                        .setExceptionHandler(this::handleException)
                        .setConnectionExceptionHandler(this::handleConnectionException)
                        .useGlobalExecutor(true)
        );
    }

    public void validateTables(PluginConfiguration configuration) {
        easyDB.runTaskSync(connection -> {
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS librepremium_data(" +
                            "uuid VARCHAR(255) NOT NULL PRIMARY KEY," +
                            "premium_uuid VARCHAR(255)," +
                            "hashed_password VARCHAR(255)," +
                            "salt VARCHAR(255)," +
                            "algo VARCHAR(255)," +
                            "last_nickname VARCHAR(255) NOT NULL," +
                            "joined TIMESTAMP NULL DEFAULT NULL," +
                            "last_seen TIMESTAMP NULL DEFAULT NULL," +
                            "last_server VARCHAR(255)" +
                            ")"
            ).executeUpdate();

            ResultSet resultSet = connection.prepareStatement("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='librepremium_data' and TABLE_SCHEMA='" + configuration.getDatabaseName() + "'")
                .executeQuery();

            ArrayList<String> columns = new ArrayList<>();
            while (resultSet.next()) {
                columns.add(resultSet.getString("column_name"));
            }
            if (!columns.contains("secret"))
                connection.prepareStatement("ALTER TABLE librepremium_data ADD COLUMN secret VARCHAR(255) NULL DEFAULT NULL").executeUpdate();
            if (!columns.contains("ip"))
                connection.prepareStatement("ALTER TABLE librepremium_data ADD COLUMN ip VARCHAR(255) NULL DEFAULT NULL").executeUpdate();
            if (!columns.contains("last_authentication"))
                connection.prepareStatement("ALTER TABLE librepremium_data ADD COLUMN last_authentication TIMESTAMP NULL DEFAULT NULL").executeUpdate();
            if (!columns.contains("last_server")) {
                connection.prepareStatement("ALTER TABLE librepremium_data ADD COLUMN last_server VARCHAR(255) NULL DEFAULT NULL").executeUpdate();
            }

        });
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

            return getUserFromResult(rs);

        });
    }

    @Override
    public Collection<User> getAllUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByUUID(UUID uuid) {
        return easyDB.runFunctionSync(connection -> {
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
                var secret = rs.getString("secret");
                var ip = rs.getString("ip");
                var lastAuthentication = rs.getTimestamp("last_authentication");
                var lastServer = rs.getString("last_server");

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
                        lastSeen,
                        secret,
                        ip,
                        lastAuthentication,
                        lastServer);
            } else return null;

        });
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        return easyDB.runFunctionSync(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE premium_uuid=?");

            ps.setString(1, uuid.toString());

            var rs = ps.executeQuery();

            return getUserFromResult(rs);
        });
    }

    @Nullable
    private User getUserFromResult(ResultSet rs) throws SQLException {
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
                    lastSeen,
                    rs.getString("secret"),
                    rs.getString("ip"),
                    rs.getTimestamp("last_authentication"),
                    rs.getString("last_server"));
        } else return null;
    }

    @Override
    public void insertUser(User user) {
        easyDB.runTaskSync(connection -> {
            var ps = connection.prepareStatement("INSERT INTO librepremium_data(uuid, premium_uuid, hashed_password, salt, algo, last_nickname, joined, last_seen, secret, ip, last_authentication, last_server) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            insertToStatement(ps, user);

            ps.executeUpdate();
        });
    }

    @Override
    public void insertUsers(Collection<User> users) {
        easyDB.runTaskSync(connection -> {
            var ps = connection.prepareStatement("INSERT IGNORE INTO librepremium_data(uuid, premium_uuid, hashed_password, salt, algo, last_nickname, joined, last_seen, secret, ip, last_authentication) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            for (User user : users) {
                insertToStatement(ps, user);

                ps.addBatch();
            }

            ps.executeBatch();
        });
    }

    private void insertToStatement(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getUuid().toString());
        ps.setString(2, user.getPremiumUUID() == null ? null : user.getPremiumUUID().toString());
        ps.setString(3, user.getHashedPassword() == null ? null : user.getHashedPassword().hash());
        ps.setString(4, user.getHashedPassword() == null ? null : user.getHashedPassword().salt());
        ps.setString(5, user.getHashedPassword() == null ? null : user.getHashedPassword().algo());
        ps.setString(6, user.getLastNickname());
        ps.setTimestamp(7, user.getJoinDate());
        ps.setTimestamp(8, user.getLastSeen());
        ps.setString(9, user.getSecret());
        ps.setString(10, user.getIp());
        ps.setTimestamp(11, user.getLastAuthentication());
        ps.setString(12, user.getLastServer());
    }

    @Override
    public void updateUser(User user) {
        easyDB.runTaskSync(connection -> {
            var ps = connection.prepareStatement("UPDATE librepremium_data SET premium_uuid=?, hashed_password=?, salt=?, algo=?, last_nickname=?, joined=?, last_seen=?, secret=?, ip=?, last_authentication=?, last_server=? WHERE uuid=?");

            ps.setString(1, user.getPremiumUUID() == null ? null : user.getPremiumUUID().toString());
            ps.setString(2, user.getHashedPassword() == null ? null : user.getHashedPassword().hash());
            ps.setString(3, user.getHashedPassword() == null ? null : user.getHashedPassword().salt());
            ps.setString(4, user.getHashedPassword() == null ? null : user.getHashedPassword().algo());
            ps.setString(5, user.getLastNickname());
            ps.setTimestamp(6, user.getJoinDate());
            ps.setTimestamp(7, user.getLastSeen());
            ps.setString(8, user.getSecret());
            ps.setString(9, user.getIp());
            ps.setTimestamp(10, user.getLastAuthentication());
            ps.setString(11, user.getLastServer());
            ps.setString(12, user.getUuid().toString());
            ps.executeUpdate();
        });
    }

    @Override
    public void deleteUser(User user) {
        easyDB.runTaskSync(connection -> {
            var ps = connection.prepareStatement("DELETE FROM librepremium_data WHERE uuid=?");

            ps.setString(1, user.getUuid().toString());

            ps.executeUpdate();
        });
    }

    public void disable() {
        easyDB.stop();
    }
}
