package xyz.kyngs.librelogin.bungeecord;

import co.aikar.commands.BungeeCommandIssuer;
import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import net.byteflux.libby.BungeeLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
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
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.CorruptedConfigurationException;
import xyz.kyngs.librelogin.common.config.HoconPluginConfiguration;
import xyz.kyngs.librelogin.common.image.AuthenticImageProjector;
import xyz.kyngs.librelogin.common.image.protocolize.ProtocolizeImageProjector;
import xyz.kyngs.librelogin.common.util.CancellableTask;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.*;

public class BungeeCordLibreLogin extends AuthenticLibreLogin<ProxiedPlayer, ServerInfo> {

    private final BungeeCordBootstrap bootstrap;
    private BungeeAudiences adventure;
    @Nullable
    private RedisBungeeAPI redisBungee;
    private BungeeComponentSerializer serializer;

    public BungeeCordLibreLogin(BungeeCordBootstrap bootstrap) {
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

        var millis = getConfiguration().get(MILLISECONDS_TO_REFRESH_NOTIFICATION);

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

            @Override
            public void debug(String message) {
                if (getConfiguration().get(DEBUG)) {
                    bootstrap.getLogger().log(Level.INFO, "[DEBUG] " + message);
                }
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
    public void validateConfiguration(HoconPluginConfiguration configuration) throws CorruptedConfigurationException {
        var serverMap = bootstrap.getProxy().getServers();
        if (configuration.get(LIMBO).isEmpty()) {
            throw new CorruptedConfigurationException("No limbo servers defined!");
        }

        if (configuration.get(PASS_THROUGH).isEmpty()) {
            throw new CorruptedConfigurationException("No pass-through servers defined!");
        }

        for (String server : configuration.get(PASS_THROUGH).values()) {
            if (!serverMap.containsKey(server)) {
                throw new CorruptedConfigurationException("The supplied pass-through server %s is not configured in the proxy configuration!".formatted(server));
            }
        }

        for (String server : configuration.get(LIMBO)) {
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
    public ServerInfo chooseLobbyDefault(ProxiedPlayer player) throws NoSuchElementException {
        var passThroughServers = getConfiguration().get(PASS_THROUGH);
        var virt = player.getPendingConnection().getVirtualHost();

        getLogger().debug("Virtual host for player " + player.getDisplayName() + " is " + virt);

        var servers = virt == null ? passThroughServers.get("root") : passThroughServers.get(virt.getHostName());

        if (servers.isEmpty()) servers = passThroughServers.get("root");

        final var finalServers = servers;

        return bootstrap.getProxy().getServers().values().stream()
                .filter(server -> finalServers.contains(server.getName()))
                .filter(server -> {
                    var ping = getServerPinger().getLatestPing(server);

                    return ping != null && ping.maxPlayers() > server.getPlayers().size();
                })
                .min(Comparator.comparingInt(o -> o.getPlayers().size()))
                .orElse(null);
    }

    @Override
    public ServerInfo chooseLimboDefault() {
        var limbos = getConfiguration().get(LIMBO);
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

    @Override
    protected List<Library> customDependencies() {
        return List.of(

        );
    }

    @Override
    protected List<String> customRepositories() {
        return List.of(

        );
    }

    @Override
    protected LibraryManager provideLibraryManager() {
        return new BungeeLibraryManager(bootstrap);
    }

}
