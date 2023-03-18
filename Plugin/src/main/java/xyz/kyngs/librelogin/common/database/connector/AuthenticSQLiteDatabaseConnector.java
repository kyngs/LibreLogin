/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.database.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.kyngs.librelogin.api.database.connector.SQLiteDatabaseConnector;
import xyz.kyngs.librelogin.api.util.ThrowableFunction;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

public class AuthenticSQLiteDatabaseConnector extends AuthenticDatabaseConnector<SQLException, Connection> implements SQLiteDatabaseConnector {

    private final HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    public AuthenticSQLiteDatabaseConnector(AuthenticLibreLogin<?, ?> plugin, String prefix) {
        super(plugin, prefix);

        this.hikariConfig = new HikariConfig();

        hikariConfig.setPoolName("LibreLogin SQLite Pool");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setMaxLifetime(60000);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariConfig.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + get(Configuration.PATH));
    }

    @Override
    public void connect() throws SQLException {
        dataSource = new HikariDataSource(hikariConfig);
        obtainInterface().close(); //Verify connection
        connected = true;
    }

    @Override
    public void disconnect() throws SQLException {
        connected = false;
        dataSource.close();
    }

    @Override
    public Connection obtainInterface() throws SQLException, IllegalStateException {
        if (!connected()) throw new IllegalStateException("Not connected to the database!");
        return dataSource.getConnection();
    }

    @Override
    public <V> V runQuery(ThrowableFunction<Connection, V, SQLException> function) throws IllegalStateException {
        try {
            try (var connection = obtainInterface()) {
                return function.apply(connection);
            }
        } catch (SQLTransientConnectionException e) {
            plugin.getLogger().error("!! LOST CONNECTION TO THE DATABASE, THE PROXY IS GOING TO SHUT DOWN TO PREVENT DAMAGE !!");
            e.printStackTrace();
            System.exit(1);
            //Won't return anyway
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Configuration {
        public static final ConfigurationKey<String> PATH = new ConfigurationKey<>(
                "path",
                "user-data.db",
                "Path to SQLite database file. Relative to plugin datafolder.",
                ConfigurateHelper::getString
        );
    }
}
