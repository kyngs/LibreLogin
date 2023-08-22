/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.velocity;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.VelocityCommandIssuer;
import co.aikar.commands.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bstats.charts.CustomChart;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.exception.EventCancelledException;
import xyz.kyngs.librelogin.api.integration.LimboIntegration;
import xyz.kyngs.librelogin.api.provider.LibreLoginProvider;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.SLF4JLogger;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.image.AuthenticImageProjector;
import xyz.kyngs.librelogin.common.image.protocolize.ProtocolizeImageProjector;
import xyz.kyngs.librelogin.common.util.CancellableTask;
import xyz.kyngs.librelogin.velocity.integration.VelocityNanoLimboIntegration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.DEBUG;

public class VelocityLibreLogin extends AuthenticLibreLogin<Player, RegisteredServer> implements LibreLoginProvider<Player, RegisteredServer> {

    private final VelocityBootstrap bootstrap;
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
    @Nullable
    private LimboIntegration<RegisteredServer> limboIntegration;

    public VelocityLibreLogin(VelocityBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    protected void disable() {
        super.disable();
    }

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
        return new SLF4JLogger(logger, () -> getConfiguration().get(DEBUG));
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        return new VelocityCommandManager(server, bootstrap);
    }

    @Override
    public Player getPlayerFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getPlayer();
    }

    @Override
    public void authorize(Player player, User user, Audience audience) {
        try {
            var lobby = getServerHandler().chooseLobbyServer(user, player, true, false);
            if (lobby == null) {
                player.disconnect(getMessages().getMessage("kick-no-lobby"));
                return;
            }
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
        } catch (EventCancelledException ignored) {}
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = server.getScheduler()
                .buildTask(bootstrap, runnable)
                .delay(delayInMillis, TimeUnit.MILLISECONDS)
                .schedule();
        return task::cancel;
    }

    @Override
    public CancellableTask repeat(Runnable runnable, long delayInMillis, long repeatInMillis) {
        var task = server.getScheduler()
                .buildTask(bootstrap, runnable)
                .delay(delayInMillis, TimeUnit.MILLISECONDS)
                .repeat(repeatInMillis, TimeUnit.MILLISECONDS)
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
            var projector = new ProtocolizeImageProjector<>(this);
            var maxProtocol = ProtocolVersion.MAXIMUM_VERSION.getProtocol();

            if (maxProtocol == 760) {
                // I hate this so much
                try {
                    var split = server.getVersion().getVersion().split("-");
                    var build = Integer.parseInt(split[split.length - 1].replace("b", ""));

                    if (build < 172) {
                        logger.warn("Detected protocolize, but in order for the integration to work properly, you must be running Velocity build 172 or newer!");
                        return null;
                    }
                } catch (Exception e) {
                    // I guess it's probably fine
                }
            }

            if (!projector.compatible()) {
                getLogger().warn("Detected protocolize, however, with incompatible version (2.2.2), please upgrade or downgrade.");
                return null;
            }
            getLogger().info("Detected Protocolize, enabling 2FA...");
            return projector;
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
        getLogger().info("LibreLogin version " + getVersion() + " enabled!");
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
        var metrics = factory.make(bootstrap, 17981);

        for (CustomChart chart : charts) {
            metrics.addCustomChart(chart);
        }
    }

    @Override
    public Audience getAudienceFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getIssuer();
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
    public LibreLoginPlugin<Player, RegisteredServer> getLibreLogin() {
        return this;
    }

    @Nullable
    @Override
    public LimboIntegration<RegisteredServer> getLimboIntegration() {
        if (pluginPresent("nanolimbovelocity") && limboIntegration == null) {
            limboIntegration = new VelocityNanoLimboIntegration(server,
                    getConfiguration().get(ConfigurationKeys.LIMBO_PORT_RANGE));
        }
        return limboIntegration;
    }


}
