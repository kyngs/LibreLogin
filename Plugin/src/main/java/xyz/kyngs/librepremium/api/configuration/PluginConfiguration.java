package xyz.kyngs.librepremium.api.configuration;

import xyz.kyngs.librepremium.api.LibrePremiumPlugin;

import java.io.IOException;
import java.util.Collection;

public interface PluginConfiguration {

    boolean reload(LibrePremiumPlugin plugin) throws IOException, CorruptedConfigurationException;

    Collection<String> getAllowedCommandsWhileUnauthorized();
    
    int getDatabaseCacheTime();

    String getDatabasePassword();

    String getDatabaseUser();

    String getDatabaseHost();

    String getDatabaseName();

    Collection<String> getPassThrough();

    Collection<String> getLimbo();

    int getDatabasePort();

    String getDefaultCryptoProvider();

    boolean kickOnWrongPassword();

    boolean migrationOnNextStartup();

    String getMigrationType();

    String getMigrationOldDatabaseHost();

    int getMigrationOldDatabasePort();

    String getMigrationOldDatabaseUser();

    String getMigrationOldDatabasePassword();

    String getMigrationOldDatabaseName();

    String getMigrationOldDatabaseTable();

    NewUUIDCreator getNewUUIDCreator();

    boolean useTitles();

    boolean autoRegister();

    int milliSecondsToRefreshNotification();

    int secondsToAuthorize();

}
