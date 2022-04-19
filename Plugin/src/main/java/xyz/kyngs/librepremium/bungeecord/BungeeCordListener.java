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

import static net.md_5.bungee.event.EventPriority.HIGHEST;

public class BungeeCordListener extends AuthenticListeners<BungeeCordLibrePremium> implements Listener {

    private final BungeeCordPlugin plugin;

    public BungeeCordListener(BungeeCordPlugin plugin) {
        super(plugin.getLibrePremium());
        this.plugin = plugin;
    }

    @EventHandler(priority = HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        onPostLogin(event.getPlayer().getUniqueId(), plugin.getAdventure().player(event.getPlayer()));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        onPlayerDisconnect(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = HIGHEST)
    public void onPreLogin(PreLoginEvent event) {
        event.registerIntent(plugin);

        GeneralUtil.ASYNC_POOL.execute(() -> {
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

            event.completeIntent(plugin);
        });

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileRequest(LoginEvent event) {
        var profile = plugin.getLibrePremium().getDatabaseProvider().getByName(event.getConnection().getName());
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

        ServerInfo serverInfo = plugin.getProxy().getServerInfo(chooseServer(event.getPlayer().getUniqueId(), plugin.getLibrePremium().getAudienceForSender(event.getPlayer())));

        if (serverInfo == null) {
            event.getPlayer().disconnect(plugin.getSerializer().serialize(plugin.getLibrePremium().getMessages().getMessage("kick-no-server")));
            event.setCancelled(true);
            return;
        }

        event.setTarget(serverInfo);
    }

}
