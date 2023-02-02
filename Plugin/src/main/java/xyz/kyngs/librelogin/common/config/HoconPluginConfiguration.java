package xyz.kyngs.librelogin.common.config;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librelogin.api.configuration.NewUUIDCreator;
import xyz.kyngs.librelogin.api.configuration.PluginConfiguration;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;
import xyz.kyngs.librelogin.common.config.migrate.config.FirstConfigurationMigrator;
import xyz.kyngs.librelogin.common.config.migrate.config.SecondConfigurationMigrator;
import xyz.kyngs.librelogin.common.config.migrate.config.ThirdConfigurationMigrator;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static xyz.kyngs.librelogin.common.config.DefaultConfiguration.*;

public class HoconPluginConfiguration implements PluginConfiguration {

    private ConfigurateHelper helper;
    private Duration sessionTimeout;
    private final Logger logger;

    public HoconPluginConfiguration(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean reload(LibreLoginPlugin plugin) throws IOException, CorruptedConfigurationException {
        var adept = new ConfigurateConfiguration(
                plugin.getDataFolder(),
                "config.conf",
                DefaultConfiguration.class,
                """
                          !!THIS FILE IS WRITTEN IN THE HOCON FORMAT!!
                          The hocon format is very similar to JSON, but it has some extra features.
                          You can find more information about the format on the sponge wiki:
                          https://docs.spongepowered.org/stable/en/server/getting-started/configuration/hocon.html
                          ----------------------------------------------------------------------------------------
                          LibreLogin Configuration
                          ----------------------------------------------------------------------------------------
                          This is the configuration file for LibreLogin.
                          You can find more information about LibreLogin on the github page:
                          https://github.com/kyngs/LibreLogin
                        """,
                logger, new FirstConfigurationMigrator(), new SecondConfigurationMigrator(), new ThirdConfigurationMigrator()
        );

        var helperAdept = adept.getHelper();

        if (!adept.isNewlyCreated() && plugin.getCryptoProvider(helperAdept.get(DEFAULT_CRYPTO_PROVIDER)) == null) {
            throw new CorruptedConfigurationException("Crypto provider not found");
        }

        var timeoutSecs = helperAdept.get(SESSION_TIMEOUT);

        sessionTimeout = timeoutSecs <= 0 ? null : Duration.ofSeconds(timeoutSecs);

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
    public Multimap<String, String> getPassThrough() {
        return get(PASS_THROUGH);
    }

    @Override
    public List<String> getLimbo() {
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
        return get(MIGRATION_OLD_DATABASE_PORT);
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

    @Override
    public int milliSecondsToRefreshNotification() {
        return get(MILLISECONDS_TO_REFRESH_NOTIFICATION);
    }

    @Override
    public int secondsToAuthorize() {
        return get(SECONDS_TO_AUTHORIZE);
    }

    @Override
    public boolean totpEnabled() {
        return get(TOTP_ENABLED);
    }

    @Override
    public String getTotpLabel() {
        return get(TOTP_LABEL);
    }

    @Override
    public int minimumPasswordLength() {
        return get(MINIMUM_PASSWORD_LENGTH);
    }

    @Override
    public @Nullable Duration getSessionTimeout() {
        return sessionTimeout;
    }

    @Override
    public boolean pingServers() {
        return get(PING_SERVERS);
    }

    @Override
    public boolean rememberLastServer() {
        return get(REMEMBER_LAST_SERVER);
    }

    @Override
    public int maxLifeTime() {
        return get(MAX_LIFE_TIME);
    }

    @Override
    public boolean debug() {
        return get(DEBUG);
    }

    public boolean fallback() {
        return get(FALLBACK);
    }

    public <T> T get(ConfigurationKey<T> key) {
        return helper.get(key);
    }
}
