/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.database.connector;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The SQLDatabaseConnector interface extends the DatabaseConnector interface with specific
 * implementation for SQL databases.
 * It provides methods to connect to a SQL database, handle SQLExceptions, and obtain a Connection object.
 */
public interface SQLDatabaseConnector extends DatabaseConnector<SQLException, Connection> {
}
