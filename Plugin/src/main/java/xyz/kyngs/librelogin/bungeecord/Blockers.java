/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.kyngs.librelogin.api.authorization.AuthorizationProvider;
import xyz.kyngs.librelogin.common.config.HoconPluginConfiguration;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.ALLOWED_COMMANDS_WHILE_UNAUTHORIZED;
import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.LIMBO;

public class Blockers implements Listener {

    private final AuthorizationProvider<ProxiedPlayer> authorizationProvider;
    private final HoconPluginConfiguration configuration;
    private final BungeeCordLibreLogin plugin;

    public Blockers(BungeeCordLibreLogin plugin) {
        this.authorizationProvider = plugin.getAuthorizationProvider();
        this.configuration = plugin.getConfiguration();
        this.plugin = plugin;
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

        for (String allowed : configuration.get(ALLOWED_COMMANDS_WHILE_UNAUTHORIZED)) {
            if (command.equals(allowed)) return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer()) && event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY) {
            event.setCancelled(true);
        } else if (authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            if (!configuration.get(LIMBO).contains(event.getTarget().getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer()) || authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            var reason = event.getKickReasonComponent();
            if (reason == null) {
                event.getPlayer().disconnect("Limbo not running");
            } else {
                event.getPlayer().disconnect(reason);
            }
        }
    }

}
