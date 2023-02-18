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
