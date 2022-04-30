package xyz.kyngs.librepremium.common.event;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.PlayerBasedEvent;

import java.util.UUID;

public class AuthenticPlayerBasedEvent implements PlayerBasedEvent {

    private final User user;
    private final Audience audience;
    private final UUID uuid;

    public AuthenticPlayerBasedEvent(@Nullable User user, Audience audience, UUID uuid) {
        this.user = user;
        this.audience = audience;
        this.uuid = uuid;
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
}
