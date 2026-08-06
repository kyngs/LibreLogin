/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.database.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.kyngs.librelogin.api.database.connector.MySQLDatabaseConnector;
import xyz.kyngs.librelogin.api.util.ThrowableFunction;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

public class AuthenticMySQLDatabaseConnector extends AuthenticDatabaseConnector<SQLException, Connection> implements MySQLDatabaseConnector {

    private final HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    public AuthenticMySQLDatabaseConnector(AuthenticLibreLogin<?, ?> plugin, String prefix) {
        super(plugin, prefix);

        this.hikariConfig = new HikariConfig();

        hikariConfig.setPoolName("LibreLogin MySQL Pool");
        hikariConfig.setDriverClassName("xyz.kyngs.librelogin.lib.mariadb.jdbc.Driver");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setUsername(get(Configuration.USER));
        hikariConfig.setPassword(get(Configuration.PASSWORD));
        hikariConfig.setJdbcUrl(get(Configuration.JDBC_URL)
                .replace("%host%", get(Configuration.HOST))
                .replace("%port%", String.valueOf(get(Configuration.PORT)))
                .replace("%database%", get(Configuration.NAME))
        );
        hikariConfig.setMaxLifetime(get(Configuration.MAX_LIFE_TIME));
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

        public static final ConfigurationKey<String> HOST = new ConfigurationKey<>(
                "host",
                "localhost",
                "The host of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<String> NAME = new ConfigurationKey<>(
                "database",
                "librelogin",
                "The name of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<String> PASSWORD = new ConfigurationKey<>(
                "password",
                "",
                "The password of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<Integer> PORT = new ConfigurationKey<>(
                "port",
                3306,
                "The port of the database.",
                ConfigurateHelper::getInt
        );

        public static final ConfigurationKey<String> USER = new ConfigurationKey<>(
                "user",
                "root",
                "The user of the database.",
                ConfigurateHelper::getString
        );

        public static final ConfigurationKey<Integer> MAX_LIFE_TIME = new ConfigurationKey<>(
                "max-life-time",
                600000,
                "The maximum lifetime of a database connection in milliseconds. Don't touch this if you don't know what you're doing.",
                ConfigurateHelper::getInt
        );

        public static final ConfigurationKey<String> JDBC_URL = new ConfigurationKey<>(
                "jdbc-url",
                "jdbc:mariadb://%host%:%port%/%database%?autoReconnect=true&zeroDateTimeBehavior=convertToNull",
                "The JDBC URL of the database. Don't touch this if you don't know what you're doing. (Using jdbc:mariadb also works for pure mysql)",
                ConfigurateHelper::getString
        );
    }
}
