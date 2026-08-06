/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.database.connector;

/**
 * This interface represents a connector for interacting with a SQLite database.
 *
 * <p>
 * The interface extends the {@link SQLDatabaseConnector} interface, which provides
 * methods for executing SQL queries and managing database connections.
 * </p>
 *
 * <p>
 * The implementation of this interface should provide methods specifically for
 * interacting with a SQLite database.
 * </p>
 *
 * @see SQLDatabaseConnector
 */
public interface SQLiteDatabaseConnector extends SQLDatabaseConnector {
}
