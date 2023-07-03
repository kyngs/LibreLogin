/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.database.provider;

import xyz.kyngs.librelogin.api.database.connector.PostgreSQLDatabaseConnector;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibreLoginPostgreSQLDatabaseProvider extends LibreLoginSQLDatabaseProvider {
    public LibreLoginPostgreSQLDatabaseProvider(PostgreSQLDatabaseConnector connector, AuthenticLibreLogin<?, ?> plugin) {
        super(connector, plugin);
    }

    @Override
    protected List<String> getColumnNames(Connection connection) throws SQLException {
        var resultSet = connection.prepareStatement("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='librepremium_data' and TABLE_SCHEMA='public'")
                .executeQuery();

        var columns = new ArrayList<String>();
        while (resultSet.next()) {
            columns.add(resultSet.getString("column_name"));
        }

        return columns;
    }

    @Override
    protected String getIgnoreSuffix() {
        return " ON CONFLICT DO NOTHING";
    }

    @Override
    protected String addUnique(String column) {
        return "CREATE UNIQUE INDEX %s_index ON librepremium_data(%s)".formatted(column, column);
    }
}
