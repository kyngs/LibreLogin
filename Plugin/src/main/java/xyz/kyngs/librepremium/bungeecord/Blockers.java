package xyz.kyngs.librepremium.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.kyngs.librepremium.api.authorization.AuthorizationProvider;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;

public class Blockers implements Listener {

    private final AuthorizationProvider<ProxiedPlayer> authorizationProvider;
    private final PluginConfiguration configuration;

    public Blockers(AuthorizationProvider<ProxiedPlayer> authorizationProvider, PluginConfiguration configuration) {
        this.authorizationProvider = authorizationProvider;
        this.configuration = configuration;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(ChatEvent event) {
        if (event.isCommand()) {
            onCommand(event);
            return;
        }

        if (event.getSender() instanceof ProxiedPlayer player) {
            if (!authorizationProvider.isAuthorized(player) || authorizationProvider.isAwaiting2FA(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;
        if (authorizationProvider.isAuthorized(player) && !authorizationProvider.isAwaiting2FA(player))
            return;

        var command = event.getMessage().substring(1).split(" ")[0];

        for (String allowed : configuration.getAllowedCommandsWhileUnauthorized()) {
            if (command.startsWith(allowed)) return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent event) {
        if (authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            if (!configuration.getLimbo().contains(event.getTarget().getName())) {
                event.setCancelled(true);
            }
        }
    }

}
