package xyz.kyngs.librepremium.common.event;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.PlayerBasedEvent;

import java.util.UUID;

public class AuthenticPlayerBasedEvent implements PlayerBasedEvent {

    private final User user;
    private final Audience audience;

    public AuthenticPlayerBasedEvent(User user, Audience audience) {
        this.user = user;
        this.audience = audience;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public UUID getUUID() {
        return user.getUuid();
    }

    @Override
    public Audience getAudience() {
        return audience;
    }
}
