package xyz.kyngs.librepremium.common;

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
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.PlatformHandle;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.database.ReadDatabaseProvider;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.database.WriteDatabaseProvider;
import xyz.kyngs.librepremium.api.event.events.LimboServerChooseEvent;
import xyz.kyngs.librepremium.api.event.events.LobbyServerChooseEvent;
import xyz.kyngs.librepremium.api.premium.PremiumException;
import xyz.kyngs.librepremium.api.premium.PremiumUser;
import xyz.kyngs.librepremium.api.server.ServerPinger;
import xyz.kyngs.librepremium.api.totp.TOTPProvider;
import xyz.kyngs.librepremium.api.util.Release;
import xyz.kyngs.librepremium.api.util.SemanticVersion;
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
import xyz.kyngs.librepremium.common.image.AuthenticImageProjector;
import xyz.kyngs.librepremium.common.integration.FloodgateIntegration;
import xyz.kyngs.librepremium.common.premium.AuthenticPremiumProvider;
import xyz.kyngs.librepremium.common.server.AuthenticServerPinger;
import xyz.kyngs.librepremium.common.server.DummyServerPinger;
import xyz.kyngs.librepremium.common.totp.AuthenticTOTPProvider;
import xyz.kyngs.librepremium.common.util.CancellableTask;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class AuthenticLibrePremium<P, S> implements LibrePremiumPlugin<P, S> {

    public static final Gson GSON = new Gson();
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd. MM. yyyy HH:mm");

    private AuthenticPremiumProvider premiumProvider;
    private final Map<String, CryptoProvider> cryptoProviders;
    private final Map<String, ReadDatabaseProvider> readProviders;
    private final Multimap<P, CancellableTask> cancelOnExit;
    private AuthenticEventProvider<P, S> eventProvider;
    private final PlatformHandle<P, S> platformHandle;
    private final Set<String> forbiddenPasswords;
    private ServerPinger<S> serverPinger;
    private TOTPProvider totpProvider;
    private AuthenticImageProjector<P, S> imageProjector;
    private FloodgateIntegration floodgateApi;
    private SemanticVersion version;
    private Logger logger;
    private HoconPluginConfiguration configuration;
    private HoconMessages messages;
    private AuthenticAuthorizationProvider<P, S> authorizationProvider;
    private MySQLDatabaseProvider databaseProvider;
    private CommandProvider<P, S> commandProvider;

    protected AuthenticLibrePremium() {
        cryptoProviders = new HashMap<>();
        readProviders = new HashMap<>();
        platformHandle = providePlatformHandle();
        forbiddenPasswords = new HashSet<>();
        cancelOnExit = HashMultimap.create();
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
        var length = password.length() >= configuration.minimumPasswordLength();

        if (!length) {
            return false;
        }

        var upper = password.toUpperCase();

        return !forbiddenPasswords.contains(upper);
    }

    @Override
    public Collection<ReadDatabaseProvider> getReadProviders() {
        return readProviders.values();
    }

    @Override
    public void registerReadProvider(ReadDatabaseProvider provider, String id) {
        readProviders.put(id, provider);
    }

    public CommandProvider<P, S> getCommandProvider() {
        return commandProvider;
    }

    @Override
    public MySQLDatabaseProvider getDatabaseProvider() {
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
                .relocate("com{}zaxxer{}hikari", "xyz{}kyngs{}librepremium{}lib{}hikari")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("mysql")
                .artifactId("mysql-connector-java")
                .version("8.0.30")
                .relocate("com{}mysql", "xyz{}kyngs{}librepremium{}lib{}mysql")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}github{}kyngs")
                .artifactId("EasyDB")
                .version("a4bdf88ee0")
                .relocate("com{}zaxxer{}hikari", "xyz{}kyngs{}librepremium{}lib{}hikari")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}github{}ben-manes{}caffeine")
                .artifactId("caffeine")
                .version("3.1.1")
                .relocate("com{}github{}benmanes{}caffeine", "xyz{}kyngs{}librepremium{}lib{}caffeine")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-hocon")
                .version("4.1.2")
                .relocate("org{}spongepowered{}configurate", "xyz{}kyngs{}librepremium{}lib{}configurate")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librepremium{}lib{}reflect")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librepremium{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-core")
                .version("4.1.2")
                .relocate("org{}spongepowered{}configurate", "xyz{}kyngs{}librepremium{}lib{}configurate")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librepremium{}lib{}reflect")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librepremium{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("io{}leangen{}geantyref")
                .artifactId("geantyref")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librepremium{}lib{}reflect")
                .version("1.3.13")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}typesafe")
                .artifactId("config")
                .version("1.4.2")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librepremium{}lib{}hocon")
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

        dependencies.forEach(libraryManager::loadLibrary);

        eventProvider = new AuthenticEventProvider<>(this);
        premiumProvider = new AuthenticPremiumProvider();
        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-256"));
        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-512"));
        registerCryptoProvider(new BCrypt2ACryptoProvider());

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
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
            logger.error("!!The messages are corrupted, please look below for further clues. If you are clueless, delete the messages and a new ones will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Loading configuration...");

        configuration = new HoconPluginConfiguration(logger);

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
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
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
            databaseProvider = new MySQLDatabaseProvider(configuration, logger);
        } catch (Exception e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY LIBREPREMIUM !!");
            logger.error("Failed to connect to the database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Successfully connected to the database");

        logger.info("Validating tables");

        try {
            databaseProvider.validateTables(configuration);
        } catch (Exception e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("Failed to validate tables! Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            logger.error("Please open an issue on or GitHub, or visit Discord support");
        }

        logger.info("Tables validated");

        logger.info("Pinging servers...");
        serverPinger = configuration.pingServers() ? new AuthenticServerPinger<>(this) : new DummyServerPinger<>();
        logger.info("Pinged servers");

        // Moved to a different class to avoid class loading issues
        GeneralUtil.checkAndMigrate(configuration, logger, this);

        imageProjector = provideImageProjector();

        if (imageProjector != null) {
            if (!configuration.totpEnabled()) {
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
            logger.warn("!! YOU ARE RUNNING A DEVELOPMENT BUILD OF LIBREPREMIUM !!");
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
            var connection = new URL("https://api.github.com/repos/kyngs/LibrePremium/releases").openConnection();

            connection.setRequestProperty("User-Agent", "LibrePremium");

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
                logger.info("You are running the latest version of LibrePremium");
            } else {
                Collections.reverse(behind);
                logger.warn("!! YOU ARE RUNNING AN OUTDATED VERSION OF LIBREPREMIUM !!");
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

    public abstract void validateConfiguration(PluginConfiguration configuration) throws CorruptedConfigurationException;

    public abstract void authorize(P player, User user, Audience audience);

    public abstract CancellableTask delay(Runnable runnable, long delayInMillis);

    public abstract boolean pluginPresent(String pluginName);

    protected abstract AuthenticImageProjector<P, S> provideImageProjector();

    public S chooseLobby(User user, P player, boolean remember) throws NoSuchElementException {

        if (remember && configuration.rememberLastServer()) {
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
        if (configuration.rememberLastServer()) {
            var server = platformHandle.getPlayersServerName(player);
            if (server == null) return;
            var user = databaseProvider.getByUUID(platformHandle.getUUIDForPlayer(player));
            if (user != null && !getConfiguration().getLimbo().contains(server)) {
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

    private void shutdownProxy(int code) {
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

}