package xyz.kyngs.librepremium.bungeecord;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.kyngs.librepremium.common.listener.AuthenticListeners;

import java.lang.reflect.Field;

public class BungeeCordListener extends AuthenticListeners<BungeeCordLibrePremium> implements Listener {

    private final BungeeCordPlugin plugin;

    public BungeeCordListener(BungeeCordPlugin plugin) {
        super(plugin.getLibrePremium());
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        onPostLogin(event.getPlayer().getUniqueId(), plugin.getAdventure().player(event.getPlayer()));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        onPlayerDisconnect(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onProfileRequest(PreLoginEvent event) {
        var result = onPreLogin(event.getConnection().getName());

        switch (result.state()) {
            case DENIED -> {
                assert result.message() != null;
                event.setCancelled(true);
                event.setCancelReason(plugin.getSerializer().serialize(result.message()));
            }
            case FORCE_ONLINE -> event.getConnection().setOnlineMode(true);
            case FORCE_OFFLINE -> event.getConnection().setOnlineMode(false);
        }

    }

    @EventHandler
    public void onLoginEvent(LoginEvent event) {
        var profile = plugin.getLibrePremium().getDatabaseProvider().getByName(event.getConnection().getName());
        PendingConnection connection = event.getConnection();

        Class<?> clazz = connection.getClass();
        try {
            Field field = clazz.getDeclaredField("uniqueId");
            field.setAccessible(true);
            field.set(connection, profile.getUuid());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void chooseServer(ServerConnectEvent event) {
        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) return;

        ServerInfo serverInfo = plugin.getProxy().getServerInfo(chooseServer(event.getPlayer().getUniqueId()));

        if (serverInfo == null) {
            event.getPlayer().disconnect(plugin.getSerializer().serialize(plugin.getLibrePremium().getMessages().getMessage("kick-no-server")));
            event.setCancelled(true);
            return;
        }

        event.setTarget(serverInfo);
    }

}
