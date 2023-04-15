/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.PaperLibraryManager;
import net.kyori.adventure.audience.Audience;
import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.SLF4JLogger;
import xyz.kyngs.librelogin.common.image.AuthenticImageProjector;
import xyz.kyngs.librelogin.common.util.CancellableTask;
import xyz.kyngs.librelogin.paper.log.LogFilter;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.DEBUG;

public class PaperLibreLogin extends AuthenticLibreLogin<Player, World> {

    private final PaperBootstrap bootstrap;
    private PaperListeners listeners;
    private boolean started;

    public PaperLibreLogin(PaperBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.started = false;
    }

    public PaperBootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return bootstrap.getResource(name);
    }

    @Override
    public File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    @Override
    public String getVersion() {
        return bootstrap.getDescription().getVersion();
    }

    @Override
    public boolean isPresent(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    @Override
    public boolean multiProxyEnabled() {
        return false;
    }

    @Override
    public Player getPlayerForUUID(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    protected PaperPlatformHandle providePlatformHandle() {
        return new PaperPlatformHandle(this);
    }

    @Override
    protected Logger provideLogger() {
        return new SLF4JLogger(bootstrap.getSLF4JLogger(), () -> getConfiguration().get(DEBUG));
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        return new PaperCommandManager(bootstrap);
    }

    @Override
    protected boolean mainThread() {
        return Bukkit.isPrimaryThread() && started;
    }

    @Override
    public Player getPlayerFromIssuer(CommandIssuer issuer) {
        var bukkitIssuer = (BukkitCommandIssuer) issuer;

        return bukkitIssuer.getPlayer();
    }

    @Override
    protected void disable() {
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().unregisterAsyncHandlers(bootstrap);
        if (getDatabaseProvider() == null) return; //Not initialized

        super.disable();
    }

    @Override
    protected void enable() {

        logger = provideLogger();

        if (Bukkit.getOnlineMode()) {
            getLogger().error("!!!The server is running in online mode! LibreLogin won't start unless you set it to false!!!");
            disable();
            return;
        }

        if (Bukkit.spigot().getSpigotConfig().getBoolean("settings.bungeecord") || Bukkit.spigot().getPaperConfig().getBoolean("settings.velocity-support.enabled")) {
            getLogger().error("!!!This server is running under a proxy, LibreLogin won't start!!!");
            getLogger().error("If you want to use LibreLogin under a proxy, place it on the proxy and remove it from the server.");
            disable();
            return;
        }

        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());

        try {
            super.enable();
        } catch (ShutdownException e) {
            return;
        }

        var provider = getEventProvider();

        provider.subscribe(AuthenticatedEvent.class, event -> {
            var player = (Player) event.getPlayer(); //Not optimal
            player.setInvisible(false);
        });

        listeners = new PaperListeners(this);

        Bukkit.getPluginManager().registerEvents(listeners, bootstrap);
        Bukkit.getPluginManager().registerEvents(new Blockers(this), bootstrap);

        started = true;
    }

    @Override
    public void authorize(Player player, User user, Audience audience) {
        try {

            var location = listeners.getSpawnLocationCache().getIfPresent(player);

            if (location == null) {
                var world = getServerHandler().chooseLobbyServer(user, player, true);
                if (world == null) {
                    throw new NoSuchElementException();
                } else {
                    location = world.getSpawnLocation();
                }
            } else {
                listeners.getSpawnLocationCache().invalidate(player);
            }

            var finalLocation = location;
            PaperUtil.runSyncAndWait(() -> player.teleportAsync(finalLocation), this);

        } catch (NoSuchElementException e) {
            getPlatformHandle().kick(player, getMessages().getMessage("kick-no-server"));
        }
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = Bukkit.getScheduler().runTaskLaterAsynchronously(bootstrap, runnable, delayInMillis / 50);
        return task::cancel;
    }

    @Override
    public CancellableTask repeat(Runnable runnable, long delayInMillis, long repeatInMillis) {
        var task = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(bootstrap, runnable, delayInMillis / 50, repeatInMillis / 50);
        return task::cancel;
    }

    @Override
    public boolean pluginPresent(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    @Override
    protected AuthenticImageProjector<Player, World> provideImageProjector() {
        return null;
    }

    @Override
    protected void initMetrics(CustomChart... charts) {
        var metrics = new Metrics(bootstrap, 17915);

        for (var chart : charts) {
            metrics.addCustomChart(chart);
        }

        var isVelocity = new SimplePie("is_velocity", () -> "Paper");

        metrics.addCustomChart(isVelocity);
    }

    @Override
    protected void shutdownProxy(int code) {
        bootstrap.disable();
        bootstrap.getServer().shutdown();
        throw new ShutdownException();
    }

    @Override
    public Audience getAudienceFromIssuer(CommandIssuer issuer) {
        return ((BukkitCommandIssuer) issuer).getIssuer();
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
        try {
            Class.forName("io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader");
            return new PaperLibraryManager(bootstrap);
        } catch (ClassNotFoundException e) {
            return new BukkitLibraryManager(bootstrap);
        }
    }
}
