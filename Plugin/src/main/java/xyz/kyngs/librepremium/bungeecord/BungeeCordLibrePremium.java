package xyz.kyngs.librepremium.bungeecord;

import co.aikar.commands.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.UUID;
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
        return plugin.getAdventure().player(plugin.getProxy().getPlayer(uuid));
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
            return getAudienceForPlayer(context.getPlayer());
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
        if (!serverMap.containsKey(configuration.getLimboServer())) {
            throw new CorruptedConfigurationException("Invalid limbo");
        }

        if (configuration.getPassThroughServers().isEmpty()) {
            throw new CorruptedConfigurationException("No pass-through servers defined!");
        }

        for (String server : configuration.getPassThroughServers()) {
            if (!serverMap.containsKey(server)) {
                throw new CorruptedConfigurationException("Pass-through server %s not configured!".formatted(server));
            }
        }
    }

    @Override
    public void authorize(UUID uuid) {
        var player = plugin.getProxy().getPlayer(uuid);

        ServerInfo serverInfo = plugin.getProxy().getServerInfo(chooseLobby());

        if (serverInfo == null) {
            player.disconnect(plugin.getSerializer().serialize(getMessages().getMessage("kick-no-server")));
            return;
        }

        player.connect(serverInfo);
    }

    @Override
    public void kick(UUID uuid, Component reason) {
        plugin.getProxy().getPlayer(uuid).disconnect(plugin.getSerializer().serialize(reason));
    }

    @Override
    public String chooseLobby() throws NoSuchElementException {
        var passThroughServers = getConfiguration().getPassThroughServers();

        return plugin.getProxy().getServers().values().stream()
                .filter(server -> passThroughServers.contains(server.getName()))
                .min(Comparator.comparingInt(o -> o.getPlayers().size()))
                .orElseThrow().getName();
    }

    public Audience getAudienceForPlayer(ProxiedPlayer player) {
        return plugin.getAdventure().player(player);
    }

}
