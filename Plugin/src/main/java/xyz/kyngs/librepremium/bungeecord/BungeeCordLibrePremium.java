package xyz.kyngs.librepremium.bungeecord;

import co.aikar.commands.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeCordLibrePremium extends AuthenticLibrePremium {

    private final BungeeCordPlugin plugin;

    public BungeeCordLibrePremium(BungeeCordPlugin plugin) {
        this.plugin = plugin;
    }

    public void makeEnabled() {
        enable();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return plugin.getResourceAsStream(name);
    }

    @Override
    public Audience getAudienceForID(UUID uuid) {
        var player = plugin.getProxy().getPlayer(uuid);

        return player == null ? null : plugin.getAdventure().player(plugin.getProxy().getPlayer(uuid));
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    protected Logger provideLogger() {
        return new Logger() {
            @Override
            public void info(String message) {
                plugin.getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                plugin.getLogger().warning(message);
            }

            @Override
            public void error(String message) {
                plugin.getLogger().log(Level.SEVERE, message);
            }
        };
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        var manager = new BungeeCommandManager(plugin);

        var contexts = manager.getCommandContexts();

        contexts.registerIssuerAwareContext(Audience.class, context -> {
            if (getCommandProvider().getLimiter().tryAndLimit(context.getIssuer().getUniqueId()))
                throw new xyz.kyngs.librepremium.common.command.InvalidCommandArgument(getMessages().getMessage("error-throttle"));
            return getAudienceForSender(context.getSender());
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
        return plugin.getAdventure().sender(issuer.getIssuer());
    }

    @Override
    public void validateConfiguration(PluginConfiguration configuration) throws CorruptedConfigurationException {
        var serverMap = plugin.getProxy().getServers();
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
    public void authorize(UUID uuid, User user, Audience audience) {
        var player = plugin.getProxy().getPlayer(uuid);

        ServerInfo serverInfo = plugin.getProxy().getServerInfo(chooseLobby(user, audience));

        if (serverInfo == null) {
            player.disconnect(plugin.getSerializer().serialize(getMessages().getMessage("kick-no-server")));
            return;
        }

        player.connect(serverInfo);
    }

    @Override
    public void kick(UUID uuid, Component reason) {
        var player = plugin.getProxy().getPlayer(uuid);
        if (player == null) return;
        player.disconnect(plugin.getSerializer().serialize(reason));
    }

    @Override
    public void delay(Runnable runnable, long delayInMillis) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, delayInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    protected void initMetrics(CustomChart... charts) {
        var metrics = new Metrics(plugin, 14805);

        for (CustomChart chart : charts) {
            metrics.addCustomChart(chart);
        }

        var isVelocity = new SimplePie("using_velocity", () -> "No");

        metrics.addCustomChart(isVelocity);
    }

    @Override
    public String chooseLobbyDefault() throws NoSuchElementException {
        var passThroughServers = getConfiguration().getPassThrough();

        return plugin.getProxy().getServers().values().stream()
                .filter(server -> passThroughServers.contains(server.getName()))
                .min(Comparator.comparingInt(o -> o.getPlayers().size()))
                .orElseThrow().getName();
    }

    @Override
    public String chooseLimboDefault() {
        var limbos = getConfiguration().getLimbo();
        return plugin.getProxy().getServers().values().stream()
                .filter(server -> limbos.contains(server.getName()))
                .min(Comparator.comparingInt(o -> o.getPlayers().size()))
                .orElseThrow().getName();
    }

    public Audience getAudienceForSender(CommandSender sender) {
        return plugin.getAdventure().sender(sender);
    }

}
