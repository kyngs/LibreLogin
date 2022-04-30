package xyz.kyngs.librepremium.common.event.events;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librepremium.common.event.AuthenticPlayerBasedEvent;

public class AuthenticPremiumLoginSwitchEvent extends AuthenticPlayerBasedEvent implements PremiumLoginSwitchEvent {
    public AuthenticPremiumLoginSwitchEvent(User user, Audience audience) {
        super(user, audience, user.getUuid());
    }
}
