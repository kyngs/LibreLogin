package xyz.kyngs.librepremium.velocity;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.VelocityCommandIssuer;
import co.aikar.commands.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.PlatformHandle;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.provider.LibrePremiumProvider;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.SL4JLogger;
import xyz.kyngs.librepremium.common.image.AuthenticImageProjector;
import xyz.kyngs.librepremium.common.util.CancellableTask;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Plugin(
        id = "librepremium",
        name = "LibrePremium",
        version = "@version@",
        authors = "kyngs",
        dependencies = {
                @Dependency(id = "floodgate", optional = true),
                @Dependency(id = "protocolize", optional = true),
                @Dependency(id = "redisbungee", optional = true)
        }
)
public class VelocityLibrePremium extends AuthenticLibrePremium<Player, RegisteredServer> implements LibrePremiumProvider<Player, RegisteredServer> {

    @Inject
    private org.slf4j.Logger logger;
    @Inject
    @DataDirectory
    private Path dataDir;
    @Inject
    private ProxyServer server;
    @Inject
    private Metrics.Factory factory;
    @Inject
    private PluginDescription description;
    @Nullable
    private VelocityRedisBungeeIntegration redisBungee;

    public ProxyServer getServer() {
        return server;
    }

    static {
        System.setProperty("auth.forceSecureProfiles", "false");
    }

    @Override
    protected PlatformHandle<Player, RegisteredServer> providePlatformHandle() {
        return new VelocityPlatformHandle(this);
    }

    @Override
    protected Logger provideLogger() {
        return new SL4JLogger(logger);
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        return new VelocityCommandManager(server, this);
    }

    @Override
    public Player getPlayerFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getPlayer();
    }

    @Override
    public void validateConfiguration(PluginConfiguration configuration) throws CorruptedConfigurationException {
        if (configuration.getLimbo().isEmpty())
            throw new CorruptedConfigurationException("No limbo servers defined!");
        if (configuration.getPassThrough().isEmpty())
            throw new CorruptedConfigurationException("No pass-through servers defined!");
        for (String server : configuration.getPassThrough()) {
            if (this.server.getServer(server).isEmpty())
                throw new CorruptedConfigurationException("The supplied pass-through server is not configured in the proxy configuration!");
        }
        for (String server : configuration.getLimbo()) {
            if (this.server.getServer(server).isEmpty())
                throw new CorruptedConfigurationException("The supplied limbo server is not configured in the proxy configuration!");
        }
    }

    @Override
    public void authorize(Player player, User user, Audience audience) {
        try {
            var lobby = chooseLobby(user, player);
            if (lobby == null) throw new NoSuchElementException();
            player
                    .createConnectionRequest(
                            lobby
                    )
                    .connect()
                    .whenComplete((result, throwable) -> {
                        if (player.getCurrentServer().isEmpty()) return;
                        if (player.getCurrentServer().get().getServerInfo().getName().equals(result.getAttemptedConnection().getServerInfo().getName()))
                            return;
                        if (throwable != null || !result.isSuccessful())
                            player.disconnect(Component.text("Unable to connect"));
                    });
        } catch (NoSuchElementException e) {
            player.disconnect(getMessages().getMessage("kick-no-server"));
        }
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = server.getScheduler()
                .buildTask(this, runnable)
                .delay(delayInMillis, TimeUnit.MILLISECONDS)
                .schedule();
        return task::cancel;
    }

    @Override
    public boolean pluginPresent(String pluginName) {
        return server.getPluginManager().getPlugin(pluginName).isPresent();
    }

    @Override
    protected AuthenticImageProjector<Player, RegisteredServer> provideImageProjector() {
        if (pluginPresent("protocolize")) {
            getLogger().info("Detected Protocolize, however, due to a bug in Protocolize for Velocity it cannot be used. This bug will get resolved ASAP.");
            return null; //new ProtocolizeImageProjector<>(this);
        } else {
            logger.warn("Protocolize not found, some features (e.g. 2FA) will not work!");
            return null;
        }
    }

    @Override
    protected void enable() {
        if (pluginPresent("redisbungee")) {
            redisBungee = new VelocityRedisBungeeIntegration();
        }
        super.enable();
    }

    @Override
    public String getVersion() {
        return description.getVersion().orElseThrow();
    }

    @Override
    public boolean isPresent(UUID uuid) {
        return redisBungee != null ? redisBungee.isPlayerOnline(uuid) : getPlayerForUUID(uuid) != null;
    }

    @Override
    public boolean multiProxyEnabled() {
        return redisBungee != null;
    }

    @Override
    public Player getPlayerForUUID(UUID uuid) {
        return server.getPlayer(uuid).orElse(null);
    }

    @Override
    protected void initMetrics(CustomChart... charts) {
        var metrics = factory.make(this, 14805);

        for (CustomChart chart : charts) {
            metrics.addCustomChart(chart);
        }

        var isVelocity = new SimplePie("using_velocity", () -> "Yes");

        metrics.addCustomChart(isVelocity);
    }

    @Override
    public RegisteredServer chooseLobbyDefault() throws NoSuchElementException {
        var passThroughServers = getConfiguration().getPassThrough();
        return server.getAllServers().stream()
                .filter(server -> passThroughServers.contains(server.getServerInfo().getName()))
                .filter(server -> {
                    var ping = getServerPinger().getLatestPing(server);

                    return ping != null && ping.maxPlayers() > server.getPlayersConnected().size();
                })
                .min(Comparator.comparingInt(o -> o.getPlayersConnected().size()))
                .orElse(null);
    }

    @Override
    public RegisteredServer chooseLimboDefault() {
        var limbos = getConfiguration().getLimbo();
        return server.getAllServers().stream()
                .filter(server -> limbos.contains(server.getServerInfo().getName()))
                .filter(server -> {
                    var ping = getServerPinger().getLatestPing(server);

                    return ping != null && ping.maxPlayers() > server.getPlayersConnected().size();
                })
                .min(Comparator.comparingInt(o -> o.getPlayersConnected().size()))
                .orElse(null);
    }

    @Override
    public Audience getAudienceFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getIssuer();
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        enable();

        server.getEventManager().register(this, new Blockers(getAuthorizationProvider(), getConfiguration(), getMessages()));
        server.getEventManager().register(this, new VelocityListeners(this));

        var millis = getConfiguration().milliSecondsToRefreshNotification();

        if (millis > 0) {
            server.getScheduler().buildTask(this, () -> getAuthorizationProvider().notifyUnauthorized())
                    .repeat(getConfiguration().milliSecondsToRefreshNotification(), TimeUnit.MILLISECONDS)
                    .schedule();
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        disable();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public File getDataFolder() {
        return dataDir.toFile();
    }

    @Override
    public LibrePremiumPlugin<Player, RegisteredServer> getLibrePremium() {
        return this;
    }
}
