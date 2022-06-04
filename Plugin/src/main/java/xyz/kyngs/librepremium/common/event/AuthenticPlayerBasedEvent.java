package xyz.kyngs.librepremium.common.event;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.PlatformHandle;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.PlayerBasedEvent;

import java.util.UUID;

public class AuthenticPlayerBasedEvent<P, S> implements PlayerBasedEvent<P, S> {

    private final User user;
    private final Audience audience;
    private final UUID uuid;
    private final P player;
    private final LibrePremiumPlugin<P, S> plugin;
    private final PlatformHandle<P, S> platformHandle;

    public AuthenticPlayerBasedEvent(@Nullable User user, @Nullable P player, LibrePremiumPlugin<P, S> plugin) {
        this.plugin = plugin;
        this.platformHandle = plugin.getPlatformHandle();
        this.user = user;
        this.audience = player == null ? Audience.empty() : platformHandle.getAudienceForPlayer(player);
        this.uuid = player == null ? null : platformHandle.getUUIDForPlayer(player);
        this.player = player;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Audience getAudience() {
        return audience;
    }

    @Override
    public P getPlayer() {
        return player;
    }

    @Override
    public LibrePremiumPlugin<P, S> getPlugin() {
        return plugin;
    }

    @Override
    public PlatformHandle<P, S> getPlatformHandle() {
        return platformHandle;
    }
}
