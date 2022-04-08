package xyz.kyngs.librepremium.common.config;

import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.NewUUIDCreator;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.common.config.key.ConfigurationKey;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static xyz.kyngs.librepremium.common.config.DefaultConfiguration.*;

public class HoconPluginConfiguration implements PluginConfiguration {

    private ConfigurateHelper helper;

    @Override
    public boolean reload(LibrePremiumPlugin plugin) throws IOException, CorruptedConfigurationException {
        var adept = new ConfigurateConfiguration(
                plugin.getDataFolder(),
                "config.conf",
                DefaultConfiguration.DEFAULT_CONFIGURATION
        );

        var helperAdept = adept.getHelper();

        if (!adept.isNewlyCreated() && plugin.getCryptoProvider(helperAdept.get(DEFAULT_CRYPTO_PROVIDER)) == null) {
            throw new CorruptedConfigurationException("Crypto provider not found");
        }

        helper = helperAdept;

        return adept.isNewlyCreated();
    }

    @Override
    public List<String> getAllowedCommandsWhileUnauthorized() {
        return get(ALLOWED_COMMANDS_WHILE_UNAUTHORIZED);
    }

    @Override
    public String getDatabasePassword() {
        return get(DATABASE_PASSWORD);
    }

    @Override
    public String getDatabaseUser() {
        return get(DATABASE_USER);
    }

    @Override
    public String getDatabaseHost() {
        return get(DATABASE_HOST);
    }

    @Override
    public String getDatabaseName() {
        return get(DATABASE_NAME);
    }

    @Override
    public Collection<String> getPassThrough() {
        return get(PASS_THROUGH);
    }

    @Override
    public String getLimbo() {
        return get(LIMBO);
    }

    @Override
    public int getDatabasePort() {
        return get(DATABASE_PORT);
    }

    @Override
    public String getDefaultCryptoProvider() {
        return get(DEFAULT_CRYPTO_PROVIDER);
    }

    @Override
    public boolean kickOnWrongPassword() {
        return get(KICK_ON_WRONG_PASSWORD);
    }

    @Override
    public boolean migrationOnNextStartup() {
        return get(MIGRATION_ON_NEXT_STARTUP);
    }

    @Override
    public String getMigrationType() {
        return get(MIGRATION_TYPE);
    }

    @Override
    public String getMigrationOldDatabaseHost() {
        return get(MIGRATION_OLD_DATABASE_HOST);
    }

    @Override
    public int getMigrationOldDatabasePort() {
        return get(DATABASE_PORT);
    }

    @Override
    public String getMigrationOldDatabaseUser() {
        return get(MIGRATION_OLD_DATABASE_USER);
    }

    @Override
    public String getMigrationOldDatabasePassword() {
        return get(MIGRATION_OLD_DATABASE_PASSWORD);
    }

    @Override
    public String getMigrationOldDatabaseName() {
        return get(MIGRATION_OLD_DATABASE_NAME);
    }

    @Override
    public String getMigrationOldDatabaseTable() {
        return get(MIGRATION_OLD_DATABASE_TABLE);
    }

    @Override
    public NewUUIDCreator getNewUUIDCreator() {
        var name = get(NEW_UUID_CREATOR);

        try {
            return NewUUIDCreator.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NewUUIDCreator.RANDOM;
        }
    }

    @Override
    public boolean useTitles() {
        return get(USE_TITLES);
    }

    @Override
    public boolean autoRegister() {
        return get(AUTO_REGISTER);
    }

    public <T> T get(ConfigurationKey<T> key) {
        return helper.get(key);
    }
}
