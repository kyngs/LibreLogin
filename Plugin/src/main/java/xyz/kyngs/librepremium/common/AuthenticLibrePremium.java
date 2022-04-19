package xyz.kyngs.librepremium.common;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bstats.charts.CustomChart;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.EasyDBConfig;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.easydb.provider.mysql.MySQLConfig;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.database.ReadDatabaseProvider;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.database.WriteDatabaseProvider;
import xyz.kyngs.librepremium.api.event.events.LimboServerChooseEvent;
import xyz.kyngs.librepremium.api.event.events.LobbyServerChooseEvent;
import xyz.kyngs.librepremium.api.premium.PremiumException;
import xyz.kyngs.librepremium.api.premium.PremiumUser;
import xyz.kyngs.librepremium.common.authorization.AuthenticAuthorizationProvider;
import xyz.kyngs.librepremium.common.command.CommandProvider;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;
import xyz.kyngs.librepremium.common.config.HoconMessages;
import xyz.kyngs.librepremium.common.config.HoconPluginConfiguration;
import xyz.kyngs.librepremium.common.crypto.BCrypt2ACryptoProvider;
import xyz.kyngs.librepremium.common.crypto.MessageDigestCryptoProvider;
import xyz.kyngs.librepremium.common.database.MySQLDatabaseProvider;
import xyz.kyngs.librepremium.common.event.AuthenticEventProvider;
import xyz.kyngs.librepremium.common.event.events.AuthenticLimboServerChooseEvent;
import xyz.kyngs.librepremium.common.event.events.AuthenticLobbyServerChooseEvent;
import xyz.kyngs.librepremium.common.migrate.AegisReadProvider;
import xyz.kyngs.librepremium.common.migrate.AuthMeReadProvider;
import xyz.kyngs.librepremium.common.migrate.DBAReadProvider;
import xyz.kyngs.librepremium.common.migrate.JPremiumReadProvider;
import xyz.kyngs.librepremium.common.service.mojang.MojangPremiumProvider;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class AuthenticLibrePremium implements LibrePremiumPlugin {

    public static final Gson GSON = new Gson();
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd. MM. yyyy HH:mm");

    private final MojangPremiumProvider premiumProvider;
    private final Map<String, CryptoProvider> cryptoProviders;
    private final Map<String, ReadDatabaseProvider> readProviders;
    private final AuthenticEventProvider eventProvider;
    private Logger logger;
    private HoconPluginConfiguration configuration;
    private HoconMessages messages;
    private AuthenticAuthorizationProvider authorizationProvider;
    private MySQLDatabaseProvider databaseProvider;
    private CommandProvider commandProvider;

    protected AuthenticLibrePremium() {
        premiumProvider = new MojangPremiumProvider();
        cryptoProviders = new HashMap<>();
        readProviders = new HashMap<>();
        eventProvider = new AuthenticEventProvider(this);

        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-256"));
        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-512"));
        registerCryptoProvider(new BCrypt2ACryptoProvider());
    }

    @Override
    public Collection<ReadDatabaseProvider> getReadProviders() {
        return readProviders.values();
    }

    @Override
    public void registerReadProvider(ReadDatabaseProvider provider, String id) {
        readProviders.put(id, provider);
    }

    public CommandProvider getCommandProvider() {
        return commandProvider;
    }

    @Override
    public MySQLDatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    @Override
    public MojangPremiumProvider getPremiumProvider() {
        return premiumProvider;
    }

    protected void enable() {
        logger = provideLogger();

        logger.info("Loading configuration...");

        checkDataFolder();

        configuration = new HoconPluginConfiguration();

        try {
            if (configuration.reload(this)) {
                logger.warn("!! A new configuration was generated, please fill it out, if in doubt, see the wiki !!");
                System.exit(0);
            }

            validateConfiguration(configuration);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the configuration, this most likely isn't your fault");
            System.exit(1);
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
            logger.error("!!The configuration is corrupted, please look below for further clues. If you are clueless, delete the config and a new one will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            System.exit(1);
        }

        logger.info("Configuration loaded");

        logger.info("Loading messages...");

        messages = new HoconMessages();

        try {
            messages.reload(this);
        } catch (IOException e) {
            logger.info("An unknown exception occurred while attempting to load the messages, this most likely isn't your fault");
            throw new RuntimeException(e);
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
            logger.error("!!The messages are corrupted, please look below for further clues. If you are clueless, delete the messages and a new ones will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            System.exit(1);
        }

        logger.info("Messages loaded");

        logger.info("Connecting to the database...");

        try {
            databaseProvider = new MySQLDatabaseProvider(configuration, logger);
        } catch (Exception e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
            logger.error("Failed to connect to the database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            System.exit(1);
        }

        logger.info("Successfully connected to the database");

        logger.info("Validating tables");

        try {
            databaseProvider.validateTables();
        } catch (Exception e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("Failed to validate tables! Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            logger.error("Please open an issue on or GitHub, or visit Discord support");
        }

        logger.info("Tables validated");

        checkAndMigrate();

        authorizationProvider = new AuthenticAuthorizationProvider(this);
        commandProvider = new CommandProvider(this);

        if (getVersion().contains("DEVELOPMENT")) {
            logger.warn("!! YOU ARE RUNNING A DEVELOPMENT BUILD OF LIBREPREMIUM !!");
            logger.warn("!! THIS IS NOT A RELEASE, USE THIS ONLY IF YOU WERE INSTRUCTED TO DO SO. DO NOT USE THIS IN PRODUCTION !!");
        } else {
            delay(this::checkForUpdates, 1000);
            initMetrics();
        }

    }

    private void checkForUpdates() {
        logger.info("Checking for updates...");
        try (var in = new URL("https://api.github.com/repos/kyngs/LibrePremium/releases/latest").openStream()) {

            var root = GSON.fromJson(new InputStreamReader(in), JsonObject.class);

            var version = root.get("tag_name").getAsString();

            if (version.equals(getVersion())) {
                logger.info("You are running the latest version of LibrePremium");
            } else {
                logger.warn("!! YOU ARE RUNNING AN OUTDATED VERSION OF LIBREPREMIUM !!");
                logger.info("You are running version %s, the latest version is %s".formatted(getVersion(), version));
                logger.info("Latest version name: %s".formatted(root.get("name").getAsString()));
                logger.warn("!! PLEASE UPDATE TO THE LATEST VERSION !!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Failed to check for updates");
        }
    }

    private void checkAndMigrate() {
        if (configuration.migrationOnNextStartup()) {
            logger.info("Performing migration...");

            try {
                logger.info("Connecting to the OLD database...");

                EasyDB<MySQL, Connection, SQLException> easyDB;

                try {
                    easyDB = new EasyDB<>(
                            new EasyDBConfig<>(
                                    new MySQL(
                                            new MySQLConfig()
                                                    .setUsername(configuration.getMigrationOldDatabaseUser())
                                                    .setPassword(configuration.getMigrationOldDatabasePassword())
                                                    .setJdbcUrl("jdbc:mysql://%s:%s/%s?autoReconnect=true".formatted(configuration.getMigrationOldDatabaseHost(), configuration.getMigrationOldDatabasePort(), configuration.getMigrationOldDatabaseName()))
                                    )
                            )
                                    .useGlobalExecutor(true)
                    );

                    logger.info("Connected to the OLD database");

                } catch (Exception e) {
                    var cause = GeneralUtil.getFurthestCause(e);
                    logger.error("!! THIS IS NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
                    logger.error("Failed to connect to the OLD database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
                    logger.error("Aborting migration");

                    return;
                }

                try {
                    var localProviders = new HashMap<String, ReadDatabaseProvider>();

                    localProviders.put("JPremium", new JPremiumReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));
                    localProviders.put("AuthMe", new AuthMeReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));
                    localProviders.put("Aegis", new AegisReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));
                    localProviders.put("DBA-SHA-512", new DBAReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));

                    var provider = localProviders.get(configuration.getMigrationType());

                    if (provider == null) {
                        logger.error("Unknown migrator %s, aborting migration".formatted(configuration.getMigrationType()));
                        return;
                    }

                    logger.info("Starting data conversion... This may take a while!");

                    migrate(provider, databaseProvider);

                    logger.info("Migration complete, cleaning up!");

                } finally {
                    easyDB.stop();
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("An unexpected exception occurred while performing database migration, aborting migration");
            }

        }
    }

    public UUID generateNewUUID(String name, @Nullable UUID premiumID) {
        return switch (configuration.getNewUUIDCreator()) {
            case RANDOM -> UUID.randomUUID();
            case MOJANG -> premiumID == null ? GeneralUtil.getCrackedUUIDFromName(name) : premiumID;
            case CRACKED -> GeneralUtil.getCrackedUUIDFromName(name);
        };
    }

    protected void disable() {
        databaseProvider.disable();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public PluginConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Messages getMessages() {
        return messages;
    }

    @Override
    public void checkDataFolder() {
        var folder = getDataFolder();

        if (!folder.exists()) if (!folder.mkdir()) throw new RuntimeException("Failed to create datafolder");
    }

    protected abstract Logger provideLogger();

    public abstract CommandManager<?, ?, ?, ?, ?, ?> provideManager();

    public abstract Audience getFromIssuer(CommandIssuer issuer);

    public abstract void validateConfiguration(PluginConfiguration configuration) throws CorruptedConfigurationException;

    public abstract void authorize(UUID uuid, User user, Audience audience);

    public abstract void kick(UUID uuid, Component reason);

    public abstract void delay(Runnable runnable, long delayInMillis);

    public String chooseLobby(User user, Audience audience) throws NoSuchElementException {
        var event = new AuthenticLobbyServerChooseEvent(user, audience);

        getEventProvider().fire(LobbyServerChooseEvent.class, event);

        return event.getServer() != null ? event.getServer() : chooseLobbyDefault();
    }

    public PremiumUser getUserOrThrowICA(String username) throws InvalidCommandArgument {
        try {
            return getPremiumProvider().getUserForName(username);
        } catch (PremiumException e) {
            throw new InvalidCommandArgument(getMessages().getMessage(
                    switch (e.getIssue()) {
                        case THROTTLED -> "error-premium-throttled";
                        default -> "error-premium-unknown";
                    }
            ));
        }
    }

    protected abstract void initMetrics(CustomChart... charts);

    public abstract String chooseLobbyDefault();

    @Override
    public AuthenticAuthorizationProvider getAuthorizationProvider() {
        return authorizationProvider;
    }

    @Override
    public CryptoProvider getCryptoProvider(String id) {
        return cryptoProviders.get(id);
    }

    @Override
    public void registerCryptoProvider(CryptoProvider provider) {
        cryptoProviders.put(provider.getIdentifier(), provider);
    }

    @Override
    public CryptoProvider getDefaultCryptoProvider() {
        return getCryptoProvider(configuration.getDefaultCryptoProvider());
    }

    @Override
    public void migrate(ReadDatabaseProvider from, WriteDatabaseProvider to) {
        logger.info("Reading data...");
        var users = from.getAllUsers();
        logger.info("Data read, inserting into database...");
        to.insertUsers(users);
    }

    @Override
    public AuthenticEventProvider getEventProvider() {
        return eventProvider;
    }

    public String getLimboServer(Audience audience, User user) {
        var event = new AuthenticLimboServerChooseEvent(user, audience);

        getEventProvider().fire(LimboServerChooseEvent.class, event);

        return event.getServer() != null ? event.getServer() : chooseLimboDefault();
    }

    public abstract String chooseLimboDefault();
}
