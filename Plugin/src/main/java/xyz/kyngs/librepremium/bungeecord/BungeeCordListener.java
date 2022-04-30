package xyz.kyngs.librepremium.bungeecord;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.kyngs.librepremium.common.listener.AuthenticListeners;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static net.md_5.bungee.event.EventPriority.HIGHEST;

public class BungeeCordListener extends AuthenticListeners<BungeeCordLibrePremium> implements Listener {

    private final BungeeCordPlugin bungeePlugin;

    public BungeeCordListener(BungeeCordPlugin plugin) {
        super(plugin.getLibrePremium());
        this.bungeePlugin = plugin;
    }

    @EventHandler(priority = HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        onPostLogin(event.getPlayer().getUniqueId(), bungeePlugin.getAdventure().player(event.getPlayer()));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        onPlayerDisconnect(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = HIGHEST)
    public void onPreLogin(PreLoginEvent event) {

        if (plugin.fromFloodgate(event.getConnection().getUniqueId())) return;

        event.registerIntent(bungeePlugin);

        GeneralUtil.ASYNC_POOL.execute(() -> {
            var result = onPreLogin(event.getConnection().getName());

            switch (result.state()) {
                case DENIED -> {
                    assert result.message() != null;
                    event.setCancelled(true);
                    event.setCancelReason(bungeePlugin.getSerializer().serialize(result.message()));
                }
                case FORCE_ONLINE -> event.getConnection().setOnlineMode(true);
                case FORCE_OFFLINE -> event.getConnection().setOnlineMode(false);
            }

            event.completeIntent(bungeePlugin);
        });

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileRequest(LoginEvent event) {

        if (plugin.fromFloodgate(event.getConnection().getUniqueId())) return;

        var profile = bungeePlugin.getLibrePremium().getDatabaseProvider().getByName(event.getConnection().getName());
        PendingConnection connection = event.getConnection();

        Class<?> clazz = connection.getClass();
        try {
            Field field = clazz.getDeclaredField("uniqueId");
            field.setAccessible(true);
            field.set(connection, profile.getUuid());
        } catch (NoSuchFieldException e) {
            var logger = super.plugin.getLogger();
            logger.error("The uuid field was not found in the PendingConnection class, please report this to the developer. And attach the field summary below.");
            logger.error("-- BEGIN FIELD SUMMARY --");
            for (Field field : clazz.getDeclaredFields()) {
                logger.error(field.getType().getName() + ": " + field.getName());
            }
            logger.error("-- END FIELD SUMMARY --");
            event.setCancelled(true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler(priority = HIGHEST)
    public void chooseServer(ServerConnectEvent event) {
        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) return;

        ServerInfo serverInfo;
        try {
            serverInfo = bungeePlugin.getProxy().getServerInfo(chooseServer(event.getPlayer().getUniqueId(), bungeePlugin.getLibrePremium().getAudienceForSender(event.getPlayer())));
        } catch (NoSuchElementException e) {
            event.getPlayer().disconnect(bungeePlugin.getSerializer().serialize(bungeePlugin.getLibrePremium().getMessages().getMessage("kick-no-server")));
            event.setCancelled(true);
            return;
        }

        if (serverInfo == null) {
            event.getPlayer().disconnect(bungeePlugin.getSerializer().serialize(bungeePlugin.getLibrePremium().getMessages().getMessage("kick-no-server")));
            event.setCancelled(true);
            return;
        }

        event.setTarget(serverInfo);
    }

}
