package xyz.kyngs.librepremium.common.event.events;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.common.event.AuthenticPlayerBasedEvent;

public class AuthenticAuthenticatedEvent extends AuthenticPlayerBasedEvent implements AuthenticatedEvent {
    public AuthenticAuthenticatedEvent(User user, Audience audience) {
        super(user, audience, user.getUuid());
    }
}
