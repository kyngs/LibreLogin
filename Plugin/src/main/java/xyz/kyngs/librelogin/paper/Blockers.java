package xyz.kyngs.librelogin.paper;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import xyz.kyngs.librelogin.api.authorization.AuthorizationProvider;
import xyz.kyngs.librelogin.api.configuration.PluginConfiguration;

public class Blockers implements Listener {

    private final AuthorizationProvider<Player> authorizationProvider;
    private final PluginConfiguration configuration;
    private final PaperLibrePremium plugin;

    public Blockers(PaperLibrePremium plugin) {
        this.authorizationProvider = plugin.getAuthorizationProvider();
        this.configuration = plugin.getConfiguration();
        this.plugin = plugin;
    }

    private <E extends PlayerEvent & Cancellable> void cancelIfNeeded(E event) {
        cancelIfNeeded(event.getPlayer(), event);
    }

    private void cancelIfNeeded(Player player, Cancellable cancellable) {
        if (cancellable(player)) {
            cancellable.setCancelled(true);
        }
    }

    private boolean cancellable(Player player) {
        return !authorizationProvider.isAuthorized(player) || authorizationProvider.isAwaiting2FA(player);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        cancelIfNeeded(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (authorizationProvider.isAuthorized(event.getPlayer()) && !authorizationProvider.isAwaiting2FA(event.getPlayer()))
            return;

        var command = event.getMessage().substring(1);

        for (String allowed : configuration.getAllowedCommandsWhileUnauthorized()) {
            if (command.startsWith(allowed)) return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;
        cancelIfNeeded(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        cancelIfNeeded(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        cancelIfNeeded(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            cancelIfNeeded(player, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        cancelIfNeeded(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            cancelIfNeeded(player, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            cancelIfNeeded(player, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            cancelIfNeeded(player, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if (cancellable(event.getPlayer())) {
            event.getPlayer().setInvisible(true);
        }
    }

}
