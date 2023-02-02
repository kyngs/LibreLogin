package xyz.kyngs.librepremium.paper;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.kyori.adventure.audience.Audience;
import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.SLF4JLogger;
import xyz.kyngs.librepremium.common.image.AuthenticImageProjector;
import xyz.kyngs.librepremium.common.util.CancellableTask;
import xyz.kyngs.librepremium.paper.log.LogFilter;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class PaperLibrePremium extends AuthenticLibrePremium<Player, World> {
    public PaperBootstrap getBootstrap() {
        return bootstrap;
    }

    private final PaperBootstrap bootstrap;

    public PaperLibrePremium(PaperBootstrap bootstrap) {
        this.bootstrap = bootstrap;
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
        return new SLF4JLogger(bootstrap.getSLF4JLogger(), () -> getConfiguration().debug());
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        return new PaperCommandManager(bootstrap);
    }

    @Override
    protected boolean mainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public Player getPlayerFromIssuer(CommandIssuer issuer) {
        var bukkitIssuer = (BukkitCommandIssuer) issuer;

        return bukkitIssuer.getPlayer();
    }

    @Override
    protected void disable() {
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().unregisterAsyncHandlers(bootstrap);
        super.disable();
    }

    @Override
    protected void enable() {

        if (Bukkit.getOnlineMode()) {
            getLogger().error("!!!The server is running in online mode! LibrePremium won't start unless you set it to false!!!");
            disable();
            return;
        }

        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());

        super.enable();

        var provider = getEventProvider();

        provider.subscribe(AuthenticatedEvent.class, event -> {
            var player = (Player) event.getPlayer(); //Not optimal
            player.setInvisible(false);
        });

        Bukkit.getPluginManager().registerEvents(new PaperListeners(this), bootstrap);
        Bukkit.getPluginManager().registerEvents(new Blockers(this), bootstrap);
    }

    @Override
    public void validateConfiguration(PluginConfiguration configuration) throws CorruptedConfigurationException {
        if (configuration.getLimbo().isEmpty())
            throw new CorruptedConfigurationException("No limbo worlds defined!");
        if (configuration.getPassThrough().isEmpty())
            throw new CorruptedConfigurationException("No pass-through worlds defined!");

        for (String server : configuration.getPassThrough().values()) {
            if (Bukkit.getWorld(server) == null)
                throw new CorruptedConfigurationException("The supplied pass-through world %s does not exist! I suggest using plugins like Multiverse to create it.".formatted(server));
        }
        for (String server : configuration.getLimbo()) {
            if (Bukkit.getWorld(server) == null)
                throw new CorruptedConfigurationException("The supplied limbo world %s does not exist! I suggest using plugins like Multiverse to create it.".formatted(server));
        }
    }

    @Override
    public void authorize(Player player, User user, Audience audience) {
        try {
            var lobby = chooseLobby(user, player, true);
            if (lobby == null) throw new NoSuchElementException();
            PaperUtil.runSyncAndWait(() -> {
                player.teleportAsync(lobby.getSpawnLocation());
            }, this);

        } catch (NoSuchElementException e) {
            player.kick(getMessages().getMessage("kick-no-server"));
        }
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = Bukkit.getScheduler().runTaskLaterAsynchronously(bootstrap, runnable, delayInMillis / 50);
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
        var metrics = new Metrics(bootstrap, 14805);

        for (var chart : charts) {
            metrics.addCustomChart(chart);
        }

        var isVelocity = new SimplePie("is_velocity", () -> "Paper");

        metrics.addCustomChart(isVelocity);
    }

    @Override
    public World chooseLobbyDefault(Player player) {
        var finalServers = getConfiguration().getPassThrough().get("root");
        return Bukkit.getWorlds().stream()
                .filter(world -> finalServers.contains(world.getName()))
                .min(Comparator.comparingInt(World::getPlayerCount))
                .orElse(null);
    }

    @Override
    public World chooseLimboDefault() {
        var finalServers = getConfiguration().getLimbo();
        return Bukkit.getWorlds().stream()
                .filter(world -> finalServers.contains(world.getName()))
                .min(Comparator.comparingInt(World::getPlayerCount))
                .orElse(null);
    }

    @Override
    protected void shutdownProxy(int code) {
        bootstrap.disable();
        bootstrap.getServer().shutdown();
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
        return new BukkitLibraryManager(bootstrap);
    }
}
