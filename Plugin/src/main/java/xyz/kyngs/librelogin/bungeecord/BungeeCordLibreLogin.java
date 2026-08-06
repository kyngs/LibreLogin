/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord;

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
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.exception.EventCancelledException;
import xyz.kyngs.librelogin.api.integration.LimboIntegration;
import xyz.kyngs.librelogin.bungeecord.integration.BungeeNanoLimboIntegration;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.image.AuthenticImageProjector;
import xyz.kyngs.librelogin.common.image.protocolize.ProtocolizeImageProjector;
import xyz.kyngs.librelogin.common.util.CancellableTask;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.DEBUG;

public class BungeeCordLibreLogin extends AuthenticLibreLogin<ProxiedPlayer, ServerInfo> {

    private final BungeeCordBootstrap bootstrap;
    private BungeeAudiences adventure;
    @Nullable
    private RedisBungeeAPI redisBungee;
    @Nullable
    private LimboIntegration<ServerInfo> limboIntegration;
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
        return new BungeeCordLogger(bootstrap, () -> getConfiguration().get(DEBUG));
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
    public void authorize(ProxiedPlayer player, User user, Audience audience) {
        try {
            var server = getServerHandler().chooseLobbyServer(user, player, true, false);

            if (server != null) {
                player.connect(server);
            } else player.disconnect(serializer.serialize(getMessages().getMessage("kick-no-lobby")));
        } catch (EventCancelledException ignored) {}
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = bootstrap.getProxy().getScheduler().schedule(bootstrap, runnable, delayInMillis, TimeUnit.MILLISECONDS);
        return task::cancel;
    }

    @Override
    public CancellableTask repeat(Runnable runnable, long delayInMillis, long repeatInMillis) {
        var task = bootstrap.getProxy().getScheduler().schedule(bootstrap, runnable, delayInMillis, repeatInMillis, TimeUnit.MILLISECONDS);
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
        var metrics = new Metrics(bootstrap, 17982);

        for (CustomChart chart : charts) {
            metrics.addCustomChart(chart);
        }
    }

    @Override
    public Audience getAudienceFromIssuer(CommandIssuer issuer) {
        return adventure.sender(issuer.getIssuer());
    }

    @Override
    protected java.util.logging.Logger getSimpleLogger() {
        return bootstrap.getProxy().getLogger();
    }

    @Nullable
    @Override
    public LimboIntegration<ServerInfo> getLimboIntegration() {
        if (pluginPresent("NanoLimboBungee") && limboIntegration == null) {
            limboIntegration = new BungeeNanoLimboIntegration(bootstrap.getProxy().getPluginManager().getPlugin("NanoLimboBungee").getClass().getClassLoader(),
                    getConfiguration().get(ConfigurationKeys.LIMBO_PORT_RANGE));
        }
        return limboIntegration;
    }
}
