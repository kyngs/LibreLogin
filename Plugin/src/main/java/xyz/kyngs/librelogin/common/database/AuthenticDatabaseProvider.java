/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.database;

import xyz.kyngs.librelogin.api.database.ReadWriteDatabaseProvider;
import xyz.kyngs.librelogin.api.database.connector.DatabaseConnector;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

public abstract class AuthenticDatabaseProvider<C extends DatabaseConnector<?, ?>> implements ReadWriteDatabaseProvider {

    protected final C connector;
    protected final AuthenticLibreLogin<?, ?> plugin;

    protected AuthenticDatabaseProvider(C connector, AuthenticLibreLogin<?, ?> plugin) {
        this.connector = connector;
        this.plugin = plugin;
    }

    public void validateSchema() {
    }

}
