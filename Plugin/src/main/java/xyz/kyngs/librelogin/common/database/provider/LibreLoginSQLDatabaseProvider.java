/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.database.provider;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.database.connector.SQLDatabaseConnector;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.HoconPluginConfiguration;
import xyz.kyngs.librelogin.common.database.AuthenticDatabaseProvider;
import xyz.kyngs.librelogin.common.database.AuthenticUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class LibreLoginSQLDatabaseProvider extends AuthenticDatabaseProvider<SQLDatabaseConnector> {

    private final HoconPluginConfiguration configuration;

    public LibreLoginSQLDatabaseProvider(SQLDatabaseConnector connector, AuthenticLibreLogin<?, ?> plugin) {
        super(connector, plugin);
        this.configuration = plugin.getConfiguration();
    }

    @Override
    public Collection<User> getByIP(String ip) {
        plugin.reportMainThread();
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE ip=?");

            ps.setString(1, ip);

            var rs = ps.executeQuery();

            var users = new ArrayList<User>();

            User user;

            while ((user = getUserFromResult(rs)) != null) {
                users.add(user);
            }

            return users;
        });
    }

    @Override
    public User getByName(String name) {
        plugin.reportMainThread();
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE LOWER(last_nickname)=LOWER(?)");

            ps.setString(1, name);

            var rs = ps.executeQuery();

            return getUserFromResult(rs);

        });
    }

    @Override
    public Collection<User> getAllUsers() {
        plugin.reportMainThread();
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data");

            var rs = ps.executeQuery();

            var users = new ArrayList<User>();

            User user;

            while ((user = getUserFromResult(rs)) != null) {
                users.add(user);
            }

            return users;
        });
    }

    @Override
    public User getByUUID(UUID uuid) {
        plugin.reportMainThread();
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM librepremium_data WHERE uuid=?");

            ps.setString(1, uuid.toString());

            var rs = ps.executeQuery();

            return getUserFromResult(rs);

        });
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        plugin.reportMainThread();
        return connector.runQuery(connection -> {
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

            return new AuthenticUser(
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
        plugin.reportMainThread();
        connector.runQuery(connection -> {
            var ps = connection.prepareStatement("INSERT INTO librepremium_data(uuid, premium_uuid, hashed_password, salt, algo, last_nickname, joined, last_seen, secret, ip, last_authentication, last_server) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            insertToStatement(ps, user);

            ps.executeUpdate();
        });
    }

    @Override
    public void insertUsers(Collection<User> users) {
        plugin.reportMainThread();
        connector.runQuery(connection -> {
            var ps = connection.prepareStatement("INSERT " + getIgnoreSyntax() + " INTO librepremium_data(uuid, premium_uuid, hashed_password, salt, algo, last_nickname, joined, last_seen, secret, ip, last_authentication, last_server) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
        plugin.reportMainThread();
        connector.runQuery(connection -> {
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
        plugin.reportMainThread();
        connector.runQuery(connection -> {
            var ps = connection.prepareStatement("DELETE FROM librepremium_data WHERE uuid=?");

            ps.setString(1, user.getUuid().toString());

            ps.executeUpdate();
        });
    }

    @Override
    public void validateSchema() {
        connector.runQuery(connection -> {
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

            var columns = getColumnNames(connection);

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

    protected abstract List<String> getColumnNames(Connection connection) throws SQLException;

    protected abstract String getIgnoreSyntax();
}
