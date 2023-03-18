/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.database.provider;

import xyz.kyngs.librelogin.api.database.connector.SQLiteDatabaseConnector;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibreLoginSQLiteDatabaseProvider extends LibreLoginSQLDatabaseProvider {
    public LibreLoginSQLiteDatabaseProvider(SQLiteDatabaseConnector connector, AuthenticLibreLogin<?, ?> plugin) {
        super(connector, plugin);
    }

    @Override
    protected List<String> getColumnNames(Connection connection) throws SQLException {
        var columns = new ArrayList<String>();

        var rs = connection.prepareStatement("PRAGMA table_info(librepremium_data)").executeQuery();

        while (rs.next()) {
            columns.add(rs.getString("name"));
        }

        return columns;
    }

    @Override
    protected String getIgnoreSyntax() {
        return "OR IGNORE";
    }
}
