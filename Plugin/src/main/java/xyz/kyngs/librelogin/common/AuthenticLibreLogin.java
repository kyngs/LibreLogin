package xyz.kyngs.librelogin.common;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.kyori.adventure.audience.Audience;
import org.bstats.charts.CustomChart;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.BiHolder;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.crypto.CryptoProvider;
import xyz.kyngs.librelogin.api.database.*;
import xyz.kyngs.librelogin.api.database.connector.DatabaseConnector;
import xyz.kyngs.librelogin.api.database.connector.MySQLDatabaseConnector;
import xyz.kyngs.librelogin.api.database.connector.SQLiteDatabaseConnector;
import xyz.kyngs.librelogin.api.event.events.LimboServerChooseEvent;
import xyz.kyngs.librelogin.api.event.events.LobbyServerChooseEvent;
import xyz.kyngs.librelogin.api.premium.PremiumException;
import xyz.kyngs.librelogin.api.premium.PremiumUser;
import xyz.kyngs.librelogin.api.server.ServerPinger;
import xyz.kyngs.librelogin.api.totp.TOTPProvider;
import xyz.kyngs.librelogin.api.util.Release;
import xyz.kyngs.librelogin.api.util.SemanticVersion;
import xyz.kyngs.librelogin.api.util.ThrowableFunction;
import xyz.kyngs.librelogin.common.authorization.AuthenticAuthorizationProvider;
import xyz.kyngs.librelogin.common.command.CommandProvider;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.config.CorruptedConfigurationException;
import xyz.kyngs.librelogin.common.config.HoconMessages;
import xyz.kyngs.librelogin.common.config.HoconPluginConfiguration;
import xyz.kyngs.librelogin.common.crypto.BCrypt2ACryptoProvider;
import xyz.kyngs.librelogin.common.crypto.MessageDigestCryptoProvider;
import xyz.kyngs.librelogin.common.database.AuthenticDatabaseProvider;
import xyz.kyngs.librelogin.common.database.connector.AuthenticMySQLDatabaseConnector;
import xyz.kyngs.librelogin.common.database.connector.AuthenticSQLiteDatabaseConnector;
import xyz.kyngs.librelogin.common.database.connector.DatabaseConnectorRegistration;
import xyz.kyngs.librelogin.common.database.provider.LibreLoginMySQLDatabaseProvider;
import xyz.kyngs.librelogin.common.database.provider.LibreLoginSQLiteDatabaseProvider;
import xyz.kyngs.librelogin.common.event.AuthenticEventProvider;
import xyz.kyngs.librelogin.common.event.events.AuthenticLimboServerChooseEvent;
import xyz.kyngs.librelogin.common.event.events.AuthenticLobbyServerChooseEvent;
import xyz.kyngs.librelogin.common.image.AuthenticImageProjector;
import xyz.kyngs.librelogin.common.integration.FloodgateIntegration;
import xyz.kyngs.librelogin.common.migrate.AegisSQLDatabaseProvider;
import xyz.kyngs.librelogin.common.migrate.AuthMeSQLReadProvider;
import xyz.kyngs.librelogin.common.migrate.DBASQLReadProvider;
import xyz.kyngs.librelogin.common.migrate.JPremiumSQLReadProvider;
import xyz.kyngs.librelogin.common.premium.AuthenticPremiumProvider;
import xyz.kyngs.librelogin.common.server.AuthenticServerPinger;
import xyz.kyngs.librelogin.common.server.DummyServerPinger;
import xyz.kyngs.librelogin.common.totp.AuthenticTOTPProvider;
import xyz.kyngs.librelogin.common.util.CancellableTask;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.*;

public abstract class AuthenticLibreLogin<P, S> implements LibreLoginPlugin<P, S> {

    public static final Gson GSON = new Gson();
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd. MM. yyyy HH:mm");
    public static final ExecutorService EXECUTOR;

    static {
        EXECUTOR = new ForkJoinPool(4);
    }

    private final Map<String, CryptoProvider> cryptoProviders;
    private final Map<String, ReadDatabaseProviderRegistration<?, ?, ?>> readProviders;
    private final Map<Class<?>, DatabaseConnectorRegistration<?, ?>> databaseConnectors;
    private final Multimap<P, CancellableTask> cancelOnExit;
    private final PlatformHandle<P, S> platformHandle;
    private final Set<String> forbiddenPasswords;
    private AuthenticPremiumProvider premiumProvider;
    private AuthenticEventProvider<P, S> eventProvider;
    private ServerPinger<S> serverPinger;
    private TOTPProvider totpProvider;
    private AuthenticImageProjector<P, S> imageProjector;
    private FloodgateIntegration floodgateApi;
    private SemanticVersion version;
    private Logger logger;
    private HoconPluginConfiguration configuration;
    private HoconMessages messages;
    private AuthenticAuthorizationProvider<P, S> authorizationProvider;
    private CommandProvider<P, S> commandProvider;
    private ReadWriteDatabaseProvider databaseProvider;
    private DatabaseConnector<?, ?> databaseConnector;

    protected AuthenticLibreLogin() {
        cryptoProviders = new HashMap<>();
        readProviders = new HashMap<>();
        databaseConnectors = new HashMap<>();
        platformHandle = providePlatformHandle();
        forbiddenPasswords = new HashSet<>();
        cancelOnExit = HashMultimap.create();
    }

    public Map<Class<?>, DatabaseConnectorRegistration<?, ?>> getDatabaseConnectors() {
        return databaseConnectors;
    }

    @Override
    public <E extends Exception, C extends DatabaseConnector<E, ?>> void registerDatabaseConnector(Class<?> clazz, ThrowableFunction<String, C, E> factory, String id) {
        registerDatabaseConnector(new DatabaseConnectorRegistration<>(factory, null, id), clazz);
    }

    @Override
    public void registerReadProvider(ReadDatabaseProviderRegistration<?, ?, ?> registration) {
        readProviders.put(registration.id(), registration);
    }

    public void registerDatabaseConnector(DatabaseConnectorRegistration<?, ?> registration, Class<?> clazz) {
        databaseConnectors.put(clazz, registration);
    }

    @Override
    public PlatformHandle<P, S> getPlatformHandle() {
        return platformHandle;
    }

    protected abstract PlatformHandle<P, S> providePlatformHandle();

    @Override
    public SemanticVersion getParsedVersion() {
        return version;
    }

    @Override
    public boolean validPassword(String password) {
        var length = password.length() >= configuration.get(MINIMUM_PASSWORD_LENGTH);

        if (!length) {
            return false;
        }

        var upper = password.toUpperCase();

        return !forbiddenPasswords.contains(upper);
    }

    @Override
    public Map<String, ReadDatabaseProviderRegistration<?, ?, ?>> getReadProviders() {
        return Map.copyOf(readProviders);
    }

    public CommandProvider<P, S> getCommandProvider() {
        return commandProvider;
    }

    @Override
    public ReadWriteDatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    @Override
    public AuthenticPremiumProvider getPremiumProvider() {
        return premiumProvider;
    }

    @Override
    public TOTPProvider getTOTPProvider() {
        return totpProvider;
    }

    @Override
    public AuthenticImageProjector<P, S> getImageProjector() {
        return imageProjector;
    }

    @Override
    public ServerPinger<S> getServerPinger() {
        return serverPinger;
    }

    protected void enable() {
        version = SemanticVersion.parse(getVersion());
        logger = provideLogger();

        var folder = getDataFolder();
        if (!folder.exists()) {
            var oldFolder = new File(folder.getParentFile(), folder.getName().equals("librelogin") ? "librepremium" : "LibrePremium");
            if (oldFolder.exists()) {
                logger.info("Migrating configuration and messages from old folder...");
                if (!oldFolder.renameTo(folder)) {
                    throw new RuntimeException("Can't migrate configuration and messages from old folder!");
                }
            }
        }

        logger.info("Loading libraries...");

        var libraryManager = provideLibraryManager();

        libraryManager.addMavenLocal();
        libraryManager.addMavenCentral();

        var repos = new ArrayList<>(customRepositories());

        repos.add("https://jitpack.io/");
        repos.add("https://mvn.exceptionflug.de/repository/exceptionflug-public/");

        repos.forEach(libraryManager::addRepository);

        var dependencies = new ArrayList<>(customDependencies());

        dependencies.add(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("5.0.1")
                .relocate("com{}zaxxer{}hikari", "xyz{}kyngs{}librelogin{}lib{}hikari")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("mysql")
                .artifactId("mysql-connector-java")
                .version("8.0.30")
                .relocate("com{}mysql", "xyz{}kyngs{}librelogin{}lib{}mysql")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}github{}ben-manes{}caffeine")
                .artifactId("caffeine")
                .version("3.1.1")
                .relocate("com{}github{}benmanes{}caffeine", "xyz{}kyngs{}librelogin{}lib{}caffeine")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-hocon")
                .version("4.1.2")
                .relocate("org{}spongepowered{}configurate", "xyz{}kyngs{}librelogin{}lib{}configurate")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librelogin{}lib{}reflect")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librelogin{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-core")
                .version("4.1.2")
                .relocate("org{}spongepowered{}configurate", "xyz{}kyngs{}librelogin{}lib{}configurate")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librelogin{}lib{}reflect")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librelogin{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("io{}leangen{}geantyref")
                .artifactId("geantyref")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librelogin{}lib{}reflect")
                .version("1.3.13")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}typesafe")
                .artifactId("config")
                .version("1.4.2")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librelogin{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("at{}favre{}lib")
                .artifactId("bcrypt")
                .version("0.9.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("dev{}samstevens{}totp")
                .artifactId("totp")
                .version("1.7.1")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("at{}favre{}lib")
                .artifactId("bytes")
                .version("1.5.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.40.1.0")
                .build()
        );

        dependencies.forEach(libraryManager::loadLibrary);

        eventProvider = new AuthenticEventProvider<>(this);
        premiumProvider = new AuthenticPremiumProvider(this);

        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-256"));
        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-512"));
        registerCryptoProvider(new BCrypt2ACryptoProvider());

        registerDatabaseConnector(new DatabaseConnectorRegistration<>(
                        prefix -> new AuthenticMySQLDatabaseConnector(this, prefix),
                        AuthenticMySQLDatabaseConnector.Configuration.class,
                        "mysql"
                ),
                MySQLDatabaseConnector.class);

        registerDatabaseConnector(new DatabaseConnectorRegistration<>(
                        prefix -> new AuthenticSQLiteDatabaseConnector(this, prefix),
                        AuthenticSQLiteDatabaseConnector.Configuration.class,
                        "sqlite"
                ),
                SQLiteDatabaseConnector.class);

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new LibreLoginMySQLDatabaseProvider(connector, this),
                "librelogin-mysql",
                MySQLDatabaseConnector.class
        ));

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new LibreLoginSQLiteDatabaseProvider(connector, this),
                "librelogin-sqlite",
                SQLiteDatabaseConnector.class
        ));

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AegisSQLDatabaseProvider(configuration.get(MIGRATION_OLD_DATABASE_TABLE), logger, connector),
                "aegis-mysql",
                MySQLDatabaseConnector.class
        ));

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthMeSQLReadProvider(configuration.get(MIGRATION_OLD_DATABASE_TABLE), logger, connector),
                "authme-mysql",
                MySQLDatabaseConnector.class
        ));

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthMeSQLReadProvider("authme", logger, connector),
                "authme-sqlite",
                SQLiteDatabaseConnector.class
        ));

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new DBASQLReadProvider(configuration.get(MIGRATION_OLD_DATABASE_TABLE), logger, connector),
                "dba-mysql",
                MySQLDatabaseConnector.class
        ));

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new JPremiumSQLReadProvider(configuration.get(MIGRATION_OLD_DATABASE_TABLE), logger, connector),
                "jpremium-mysql",
                MySQLDatabaseConnector.class
        ));

        checkDataFolder();

        logger.info("Loading messages...");

        messages = new HoconMessages(logger);

        try {
            messages.reload(this);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the messages, this most likely isn't your fault");
            shutdownProxy(1);
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBRELOGIN !!");
            logger.error("!!The messages are corrupted, please look below for further clues. If you are clueless, delete the messages and a new ones will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Loading configuration...");

        var defaults = new ArrayList<BiHolder<Class<?>, String>>();

        for (DatabaseConnectorRegistration<?, ?> value : databaseConnectors.values()) {
            if (value.configClass() == null) continue;
            defaults.add(new BiHolder<>(value.configClass(), "database.properties." + value.id() + "."));
            defaults.add(new BiHolder<>(value.configClass(), "migration.old-database." + value.id() + "."));
        }

        configuration = new HoconPluginConfiguration(logger, defaults);

        try {
            if (configuration.reload(this)) {
                logger.warn("!! A new configuration was generated, please fill it out, if in doubt, see the wiki !!");
                shutdownProxy(0);
            }

            validateConfiguration(configuration);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the configuration, this most likely isn't your fault");
            shutdownProxy(1);
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBRELOGIN !!");
            logger.error("!!The configuration is corrupted, please look below for further clues. If you are clueless, delete the config and a new one will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Loading forbidden passwords...");

        try {
            loadForbiddenPasswords();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the forbidden passwords, this most likely isn't your fault");
            shutdownProxy(1);
        }

        logger.info("Loaded %s forbidden passwords".formatted(forbiddenPasswords.size()));

        logger.info("Connecting to the database...");

        try {
            var registration = readProviders.get(configuration.get(DATABASE_TYPE));
            if (registration == null) {
                logger.error("Database type %s doesn't exist, please check your configuration".formatted(configuration.get(DATABASE_TYPE)));
                shutdownProxy(1);
            }

            DatabaseConnector<?, ?> connector = null;

            if (registration.databaseConnector() != null) {
                var connectorRegistration = getDatabaseConnector(registration.databaseConnector());

                if (connectorRegistration == null) {
                    logger.error("Database type %s is corrupted, please use a different one".formatted(configuration.get(DATABASE_TYPE)));
                    shutdownProxy(1);
                }

                connector = connectorRegistration.factory().apply("database.properties." + connectorRegistration.id() + ".");

                connector.connect();
            }

            var provider = registration.create(connector);

            if (provider instanceof ReadWriteDatabaseProvider casted) {
                databaseProvider = casted;
                databaseConnector = connector;
            } else {
                logger.error("Database type %s cannot be used for writing, please use a different one".formatted(configuration.get(DATABASE_TYPE)));
                shutdownProxy(1);
            }

        } catch (Exception e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBRELOGIN !!");
            logger.error("Failed to connect to the database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Successfully connected to the database");

        if (databaseProvider instanceof AuthenticDatabaseProvider<?> casted) {
            logger.info("Validating schema");

            try {
                casted.validateSchema();
            } catch (Exception e) {
                var cause = GeneralUtil.getFurthestCause(e);
                logger.error("Failed to validate schema! Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
                logger.error("Please open an issue on our GitHub, or visit Discord support");
                shutdownProxy(1);
            }

            logger.info("Schema validated");
        }

        logger.info("Pinging servers...");
        serverPinger = configuration.get(PING_SERVERS) ? new AuthenticServerPinger<>(this) : new DummyServerPinger<>();
        logger.info("Pinged servers");

        // Moved to a different class to avoid class loading issues
        GeneralUtil.checkAndMigrate(configuration, logger, this);

        imageProjector = provideImageProjector();

        if (imageProjector != null) {
            if (!configuration.get(TOTP_ENABLED)) {
                imageProjector = null;
                logger.warn("2FA is disabled in the configuration, aborting...");
            } else {
                imageProjector.enable();
            }
        }

        totpProvider = imageProjector == null ? null : new AuthenticTOTPProvider(this);

        authorizationProvider = new AuthenticAuthorizationProvider<>(this);
        commandProvider = new CommandProvider<>(this);

        if (getVersion().contains("DEVELOPMENT")) {
            logger.warn("!! YOU ARE RUNNING A DEVELOPMENT BUILD OF LIBRELOGIN !!");
            logger.warn("!! THIS IS NOT A RELEASE, USE THIS ONLY IF YOU WERE INSTRUCTED TO DO SO. DO NOT USE THIS IN PRODUCTION !!");
        } else {
            initMetrics();
        }

        delay(this::checkForUpdates, 1000);

        if (pluginPresent("floodgate")) {
            logger.info("Floodgate detected, enabling bedrock support...");
            floodgateApi = new FloodgateIntegration();
        }

        if (multiProxyEnabled()) {
            logger.info("Detected MultiProxy setup, enabling MultiProxy support...");
        }
    }

    public <C extends DatabaseConnector<?, ?>> DatabaseConnectorRegistration<?, C> getDatabaseConnector(Class<C> clazz) {
        return (DatabaseConnectorRegistration<?, C>) databaseConnectors.get(clazz);
    }

    protected abstract LibraryManager provideLibraryManager();

    private void loadForbiddenPasswords() throws IOException {
        var file = new File(getDataFolder(), "forbidden-passwords.txt");

        if (!file.exists()) {
            Files.copy(getResourceAsStream("forbidden-passwords.txt"), file.toPath());
        }

        try (var reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                forbiddenPasswords.add(line.toUpperCase(Locale.ROOT));
            }
        }
    }

    private void checkForUpdates() {
        logger.info("Checking for updates...");

        try {
            var connection = new URL("https://api.github.com/repos/kyngs/LibreLogin/releases").openConnection();

            connection.setRequestProperty("User-Agent", "LibreLogin");

            var in = connection.getInputStream();

            var root = GSON.fromJson(new InputStreamReader(in), JsonArray.class);

            in.close(); //Not the safest way, but a slight leak isn't a big deal

            List<Release> behind = new ArrayList<>();
            SemanticVersion latest = null;

            for (JsonElement raw : root) {
                var release = raw.getAsJsonObject();

                var version = SemanticVersion.parse(release.get("tag_name").getAsString());

                if (latest == null) latest = version;

                var shouldBreak = switch (this.version.compare(version)) {
                    case 0, 1 -> true;
                    default -> {
                        behind.add(new Release(version, release.get("name").getAsString()));
                        yield false;
                    }
                };

                if (shouldBreak) {
                    break;
                }
            }

            if (behind.isEmpty()) {
                logger.info("You are running the latest version of LibreLogin");
            } else {
                Collections.reverse(behind);
                logger.warn("!! YOU ARE RUNNING AN OUTDATED VERSION OF LIBRELOGIN !!");
                logger.info("You are running version %s, the latest version is %s. You are running %s versions behind. Newer versions:".formatted(getVersion(), latest, behind.size()));
                for (Release release : behind) {
                    logger.info("- %s".formatted(release.name()));
                }
                logger.warn("!! PLEASE UPDATE TO THE LATEST VERSION !!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Failed to check for updates");
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
        if (databaseConnector != null) {
            try {
                databaseConnector.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to disconnect from database, ignoring...");
            }
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public HoconPluginConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public HoconMessages getMessages() {
        return messages;
    }

    @Override
    public void checkDataFolder() {
        var folder = getDataFolder();

        if (!folder.exists()) if (!folder.mkdir()) throw new RuntimeException("Failed to create datafolder");
    }

    protected abstract Logger provideLogger();

    public abstract CommandManager<?, ?, ?, ?, ?, ?> provideManager();

    public abstract P getPlayerFromIssuer(CommandIssuer issuer);

    public abstract void validateConfiguration(HoconPluginConfiguration configuration) throws CorruptedConfigurationException;

    public abstract void authorize(P player, User user, Audience audience);

    public abstract CancellableTask delay(Runnable runnable, long delayInMillis);

    public abstract boolean pluginPresent(String pluginName);

    protected abstract AuthenticImageProjector<P, S> provideImageProjector();

    public S chooseLobby(User user, P player, boolean remember) throws NoSuchElementException {

        if (remember && configuration.get(REMEMBER_LAST_SERVER)) {
            var last = user.getLastServer();

            if (last != null) {
                var server = platformHandle.getServer(last);
                if (server != null) {
                    var ping = serverPinger.getLatestPing(server);
                    if (ping != null && ping.maxPlayers() > platformHandle.getConnectedPlayers(server)) {
                        return server;
                    }
                }
            }
        }

        var event = new AuthenticLobbyServerChooseEvent<>(user, player, this);

        getEventProvider().fire(LobbyServerChooseEvent.class, event);

        return event.getServer() != null ? event.getServer() : chooseLobbyDefault(player);
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

    public abstract S chooseLobbyDefault(P player);

    @Override
    public AuthenticAuthorizationProvider<P, S> getAuthorizationProvider() {
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
        return getCryptoProvider(configuration.get(DEFAULT_CRYPTO_PROVIDER));
    }

    @Override
    public void migrate(ReadDatabaseProvider from, WriteDatabaseProvider to) {
        logger.info("Reading data...");
        var users = from.getAllUsers();
        logger.info("Data read, inserting into database...");
        to.insertUsers(users);
    }

    @Override
    public AuthenticEventProvider<P, S> getEventProvider() {
        return eventProvider;
    }

    public S chooseLimbo(User user, P player) {
        var event = new AuthenticLimboServerChooseEvent<>(user, player, this);

        getEventProvider().fire(LimboServerChooseEvent.class, event);

        return event.getServer() != null ? event.getServer() : chooseLimboDefault();
    }

    public abstract S chooseLimboDefault();

    public void onExit(P player) {
        cancelOnExit.removeAll(player).forEach(CancellableTask::cancel);
        if (configuration.get(REMEMBER_LAST_SERVER)) {
            var server = platformHandle.getPlayersServerName(player);
            if (server == null) return;
            var user = databaseProvider.getByUUID(platformHandle.getUUIDForPlayer(player));
            if (user != null && !getConfiguration().get(LIMBO).contains(server)) {
                user.setLastServer(server);
                databaseProvider.updateUser(user);
            }
        }
    }

    public void cancelOnExit(CancellableTask task, P player) {
        cancelOnExit.put(player, task);
    }

    public boolean floodgateEnabled() {
        return floodgateApi != null;
    }

    public boolean fromFloodgate(UUID uuid) {
        return floodgateApi != null && uuid != null && floodgateApi.isFloodgateId(uuid);
    }

    protected void shutdownProxy(int code) {
        //noinspection finally
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        } finally {
            System.exit(code);
        }
    }

    public abstract Audience getAudienceFromIssuer(CommandIssuer issuer);

    protected abstract List<Library> customDependencies();

    protected abstract List<String> customRepositories();

    protected boolean mainThread() {
        return false;
    }

    public void reportMainThread() {
        if (mainThread()) {
            logger.error("AN IO OPERATION IS BEING PERFORMED ON THE MAIN THREAD! THIS IS A SERIOUS BUG!, PLEASE REPORT IT TO THE DEVELOPER OF THE PLUGIN AND ATTACH THE STACKTRACE BELOW!");
            new Throwable().printStackTrace();
        }
    }

    public boolean fromFloodgate(String username) {
        return floodgateApi != null && floodgateApi.getPlayer(username) != null;
    }
}