package xyz.kyngs.librepremium.velocity;

import co.aikar.commands.*;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.provider.LibrePremiumProvider;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.SL4JLogger;

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
        authors = "kyngs"
)
public class VelocityLibrePremium extends AuthenticLibrePremium implements LibrePremiumProvider {

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

    public ProxyServer getServer() {
        return server;
    }

    @Override
    protected Logger provideLogger() {
        return new SL4JLogger(logger);
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        var manager = new VelocityCommandManager(server, this);

        var contexts = manager.getCommandContexts();

        contexts.registerIssuerAwareContext(Audience.class, context -> {
            if (getCommandProvider().getLimiter().tryAndLimit(context.getIssuer().getUniqueId()))
                throw new xyz.kyngs.librepremium.common.command.InvalidCommandArgument(getMessages().getMessage("error-throttle"));
            return context.getSender();
        });
        contexts.registerIssuerAwareContext(UUID.class, context -> {
            var player = context.getPlayer();

            if (player == null) throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);

            return player.getUniqueId();
        });

        return manager;
    }

    @Override
    public Audience getFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getIssuer();
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
    public void authorize(UUID uuid, User user, Audience audience) {
        var player = server.getPlayer(uuid)
                .orElseThrow();

        try {
            player
                    .createConnectionRequest(
                            server.getServer(chooseLobby(user, player))
                                    .orElseThrow()
                    )
                    .connect()
                    .whenComplete((result, throwable) -> {
                        if (throwable != null || !result.isSuccessful())
                            player.disconnect(Component.text("Unable to connect"));
                    });
        } catch (NoSuchElementException e) {
            player.disconnect(getMessages().getMessage("kick-no-server"));
        }

    }

    @Override
    public void kick(UUID uuid, Component reason) {
        server.getPlayer(uuid).ifPresent(player -> player.disconnect(reason));
    }

    @Override
    public void delay(Runnable runnable, long delayInMillis) {
        server.getScheduler()
                .buildTask(this, runnable)
                .delay(delayInMillis, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Override
    public String getVersion() {
        return description.getVersion().orElseThrow();
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
    public String chooseLobbyDefault() throws NoSuchElementException {
        var passThroughServers = getConfiguration().getPassThrough();
        return server.getAllServers().stream()
                .filter(server -> passThroughServers.contains(server.getServerInfo().getName()))
                .min(Comparator.comparingInt(o -> o.getPlayersConnected().size()))
                .orElseThrow().getServerInfo().getName();
    }

    @Override
    public String chooseLimboDefault() {
        var limbos = getConfiguration().getLimbo();
        return server.getAllServers().stream()
                .filter(server -> limbos.contains(server.getServerInfo().getName()))
                .min(Comparator.comparingInt(o -> o.getPlayersConnected().size()))
                .orElseThrow().getServerInfo().getName();
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        enable();

        server.getEventManager().register(this, new Blockers(getAuthorizationProvider(), getConfiguration()));
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
    public Audience getAudienceForID(UUID uuid) {
        return server.getPlayer(uuid).orElse(null);
    }

    @Override
    public File getDataFolder() {
        return dataDir.toFile();
    }

    @Override
    public LibrePremiumPlugin getLibrePremium() {
        return this;
    }
}
