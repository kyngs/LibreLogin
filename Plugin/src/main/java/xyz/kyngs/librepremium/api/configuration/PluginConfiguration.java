package xyz.kyngs.librepremium.api.configuration;

import xyz.kyngs.librepremium.api.LibrePremiumPlugin;

import java.io.IOException;
import java.util.Collection;

public interface PluginConfiguration {

    void reload(LibrePremiumPlugin plugin) throws IOException, CorruptedConfigurationException;

    Collection<String> getAllowedCommandsWhileUnauthorized();

    String getDatabasePassword();

    String getDatabaseUsername();

    String getHost();

    String getDatabase();

    Collection<String> getPassThroughServers();

    String getLimboServer();

    int getPort();

    String getDefaultCryptoProvider();

    boolean kickOnWrongPassword();

    boolean migrateOnNextStartup();

    String getMigrator();

    String getOldDatabaseHost();

    int getOldDatabasePort();

    String getOldDatabaseUsername();

    String getOldDatabasePassword();

    String getOldDatabase();

    String getOldTable();

}
