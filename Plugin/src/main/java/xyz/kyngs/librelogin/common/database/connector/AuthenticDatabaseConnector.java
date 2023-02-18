package xyz.kyngs.librelogin.common.database.connector;

import xyz.kyngs.librelogin.api.database.connector.DatabaseConnector;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;

public abstract class AuthenticDatabaseConnector<E extends Exception, I> implements DatabaseConnector<E, I> {

    protected final AuthenticLibreLogin<?, ?> plugin;
    private final String prefix;
    protected boolean connected = true;

    protected AuthenticDatabaseConnector(AuthenticLibreLogin<?, ?> plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
    }

    @Override
    public boolean connected() {
        return connected;
    }

    public <T> T get(ConfigurationKey<T> key) {
        var value = key.getter().apply(plugin.getConfiguration().getHelper(), prefix + key.key());
        return value == null ? key.defaultValue() : value;
    }

}
