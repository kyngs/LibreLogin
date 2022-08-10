package xyz.kyngs.librepremium.bungeecord;

import co.aikar.commands.BungeeCommandIssuer;
import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.PlatformHandle;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.image.AuthenticImageProjector;
import xyz.kyngs.librepremium.common.image.protocolize.ProtocolizeImageProjector;
import xyz.kyngs.librepremium.common.util.CancellableTask;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeCordLibrePremium extends AuthenticLibrePremium<ProxiedPlayer, ServerInfo> {

    private final BungeeCordBootstrap bootstrap;
    private BungeeAudiences adventure;
    @Nullable
    private RedisBungeeAPI redisBungee;
    private BungeeComponentSerializer serializer;

    public BungeeCordLibrePremium(BungeeCordBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    protected BungeeCordBootstrap getBootstrap() {
        return bootstrap;
    }

    public BungeeComponentSerializer getSerializer() {
        return serializer;
    }

    public BungeeAudiences getAdventure() {
        return adventure;
    }

    @Override
    protected void enable() {
        this.adventure = BungeeAudiences.create(bootstrap);
        this.serializer = BungeeComponentSerializer.of(
                GsonComponentSerializer.builder().downsampleColors().emitLegacyHoverEvent().build(),
                LegacyComponentSerializer.builder().flattener(adventure.flattener()).build()
        );

        if (bootstrap.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            redisBungee = RedisBungeeAPI.getRedisBungeeApi();
        }

        super.enable();

        bootstrap.getProxy().getPluginManager().registerListener(bootstrap, new Blockers(this));
        bootstrap.getProxy().getPluginManager().registerListener(bootstrap, new BungeeCordListener(this));

        var millis = getConfiguration().milliSecondsToRefreshNotification();

        if (millis > 0) {
            bootstrap.getProxy().getScheduler().schedule(bootstrap, () -> getAuthorizationProvider().notifyUnauthorized(), 0, millis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void disable() {
        super.disable();
        if (adventure != null) {
            adventure.close();
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return bootstrap.getResourceAsStream(name);
    }

    @Override
    public File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    @Override
    protected PlatformHandle<ProxiedPlayer, ServerInfo> providePlatformHandle() {
        return new BungeeCordPlatformHandle(this);
    }

    @Override
    protected Logger provideLogger() {
        return new Logger() {
            @Override
            public void info(String message) {
                bootstrap.getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                bootstrap.getLogger().warning(message);
            }

            @Override
            public void error(String message) {
                bootstrap.getLogger().log(Level.SEVERE, message);
            }
        };
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        return new BungeeCommandManager(bootstrap);
    }

    @Override
    public ProxiedPlayer getPlayerFromIssuer(CommandIssuer issuer) {
        var bungee = (BungeeCommandIssuer) issuer;

        return bungee.getPlayer();
    }

    @Override
    public void validateConfiguration(PluginConfiguration configuration) throws CorruptedConfigurationException {
        var serverMap = bootstrap.getProxy().getServers();
        if (configuration.getLimbo().isEmpty()) {
            throw new CorruptedConfigurationException("No limbo servers defined!");
        }

        if (configuration.getPassThrough().isEmpty()) {
            throw new CorruptedConfigurationException("No pass-through servers defined!");
        }

        for (String server : configuration.getPassThrough()) {
            if (!serverMap.containsKey(server)) {
                throw new CorruptedConfigurationException("The supplied pass-through server is not configured in the proxy configuration!");
            }
        }

        for (String server : configuration.getLimbo()) {
            if (!serverMap.containsKey(server)) {
                throw new CorruptedConfigurationException("The supplied limbo server is not configured in the proxy configuration!");
            }
        }
    }

    @Override
    public void authorize(ProxiedPlayer player, User user, Audience audience) {
        var serverInfo = chooseLobby(user, player, true);

        if (serverInfo == null) {
            player.disconnect(serializer.serialize(getMessages().getMessage("kick-no-server")));
            return;
        }

        player.connect(serverInfo);
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = bootstrap.getProxy().getScheduler().schedule(bootstrap, runnable, delayInMillis, TimeUnit.MILLISECONDS);
        return task::cancel;
    }

    @Override
    public boolean pluginPresent(String pluginName) {
        return bootstrap.getProxy().getPluginManager().getPlugin(pluginName) != null;
    }

    @Override
    protected AuthenticImageProjector<ProxiedPlayer, ServerInfo> provideImageProjector() {
        if (pluginPresent("Protocolize")) {
            var projector = new ProtocolizeImageProjector<>(this);
            if (!projector.compatible()) {
                getLogger().warn("Detected protocolize, however with incompatible version (2.2.2), please upgrade or downgrade.");
                return null;
            }
            getLogger().info("Detected Protocolize, enabling 2FA...");
            return new ProtocolizeImageProjector<>(this);
        } else {
            getLogger().warn("Protocolize not found, some features (e.g. 2FA) will not work!");
            return null;
        }
    }


    @Override
    public String getVersion() {
        return bootstrap.getDescription().getVersion();
    }

    @Override
    public boolean isPresent(UUID uuid) {
        return redisBungee != null ? redisBungee.isPlayerOnline(uuid) : bootstrap.getProxy().getPlayer(uuid) != null;
    }

    @Override
    public boolean multiProxyEnabled() {
        return redisBungee != null;
    }

    @Override
    public ProxiedPlayer getPlayerForUUID(UUID uuid) {
        return bootstrap.getProxy().getPlayer(uuid);
    }

    @Override
    protected void initMetrics(CustomChart... charts) {
        var metrics = new Metrics(bootstrap, 14805);

        for (CustomChart chart : charts) {
            metrics.addCustomChart(chart);
        }

        var isVelocity = new SimplePie("using_velocity", () -> "No");

        metrics.addCustomChart(isVelocity);
    }

    @Override
    public ServerInfo chooseLobbyDefault() throws NoSuchElementException {
        var passThroughServers = getConfiguration().getPassThrough();

        return bootstrap.getProxy().getServers().values().stream()
                .filter(server -> passThroughServers.contains(server.getName()))
                .filter(server -> {
                    var ping = getServerPinger().getLatestPing(server);

                    return ping != null && ping.maxPlayers() > server.getPlayers().size();
                })
                .min(Comparator.comparingInt(o -> o.getPlayers().size()))
                .orElse(null);
    }

    @Override
    public ServerInfo chooseLimboDefault() {
        var limbos = getConfiguration().getLimbo();
        return bootstrap.getProxy().getServers().values().stream()
                .filter(server -> limbos.contains(server.getName()))
                .filter(server -> {
                    var ping = getServerPinger().getLatestPing(server);

                    return ping != null && ping.maxPlayers() > server.getPlayers().size();
                })
                .min(Comparator.comparingInt(o -> o.getPlayers().size()))
                .orElse(null);
    }

    @Override
    public Audience getAudienceFromIssuer(CommandIssuer issuer) {
        return adventure.sender(issuer.getIssuer());
    }

}
