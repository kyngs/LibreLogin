/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.listener.AuthenticListeners;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static net.md_5.bungee.event.EventPriority.HIGHEST;
import static net.md_5.bungee.event.EventPriority.LOW;

public class BungeeCordListener extends AuthenticListeners<BungeeCordLibreLogin, ProxiedPlayer, ServerInfo> implements Listener {

    public BungeeCordListener(BungeeCordLibreLogin plugin) {
        super(plugin);
    }

    @EventHandler(priority = HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        onPostLogin(event.getPlayer(), null);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        onPlayerDisconnect(event.getPlayer());
    }

    @EventHandler(priority = HIGHEST)
    public void onPreLogin(PreLoginEvent event) {

        if (plugin.fromFloodgate(event.getConnection().getUniqueId())) return;

        event.registerIntent(plugin.getBootstrap());

        GeneralUtil.ASYNC_POOL.execute(() -> {
            var result = onPreLogin(event.getConnection().getName(), event.getConnection().getAddress().getAddress());

            switch (result.state()) {
                case DENIED -> {
                    assert result.message() != null;
                    event.setCancelled(true);
                    event.setCancelReason(plugin.getSerializer().serialize(result.message()));
                }
                case FORCE_ONLINE -> event.getConnection().setOnlineMode(true);
                case FORCE_OFFLINE -> event.getConnection().setOnlineMode(false);
            }

            event.completeIntent(plugin.getBootstrap());
        });

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileRequest(LoginEvent event) {

        if (plugin.fromFloodgate(event.getConnection().getUniqueId())) return;

        var profile = plugin.getDatabaseProvider().getByName(event.getConnection().getName());
        PendingConnection connection = event.getConnection();

        Class<?> clazz = connection.getClass();
        try {
            Field field = clazz.getDeclaredField("uniqueId");
            field.setAccessible(true);
            field.set(connection, profile.getUuid());
        } catch (NoSuchFieldException e) {
            var logger = super.plugin.getLogger();
            logger.error("The uuid field was not found in the PendingConnection class, please report this to the developer. And attach the class summary below.");
            logger.error("-- BEGIN CLASS SUMMARY --");
            logger.error("Class: " + clazz.getName());
            for (Field field : clazz.getDeclaredFields()) {
                logger.error(field.getType().getName() + ": " + field.getName());
            }
            logger.error("-- END CLASS SUMMARY --");
            event.setCancelled(true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler(priority = HIGHEST)
    public void chooseServer(ServerConnectEvent event) {
        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) return;

        var server = chooseServer(event.getPlayer(), null, null);

        if (server.value() == null) {
            event.getPlayer().disconnect(plugin.getSerializer().serialize(plugin.getMessages().getMessage("kick-no-" + (server.key() ? "lobby" : "limbo"))));
        } else {
            event.setTarget(server.value());
        }
    }

    @EventHandler(priority = LOW)
    public void onKick(ServerKickEvent event) {
        var reason = plugin.getSerializer().deserialize(event.getKickReasonComponent());
        var message = plugin.getMessages().getMessage("info-kick").replaceText(builder -> builder.matchLiteral("%reason%").replacement(reason));
        var player = event.getPlayer();
        var audience = platformHandle.getAudienceForPlayer(event.getPlayer());

        if (event.getState() == ServerKickEvent.State.CONNECTED) {
            if (!plugin.getConfiguration().get(ConfigurationKeys.FALLBACK)) {
                event.setKickReasonComponent(plugin.getSerializer().serialize(message));
                event.setCancelled(false);
            } else {
                try {
                    var server = plugin.getServerHandler().chooseLobbyServer(plugin.getDatabaseProvider().getByUUID(player.getUniqueId()), player, false, true);

                    if (server == null) throw new NoSuchElementException();

                    event.setCancelled(true);
                    event.setCancelServer(server);
                } catch (NoSuchElementException e) {
                    event.setKickReasonComponent(plugin.getSerializer().serialize(message));
                    event.setCancelled(false);
                }
            }
        } else {
            audience.sendMessage(message);
        }
    }

}
