package xyz.kyngs.librepremium.common.event.events;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.LimboServerChooseEvent;
import xyz.kyngs.librepremium.common.event.AuthenticServerChooseEvent;

public class AuthenticLimboServerChooseEvent extends AuthenticServerChooseEvent implements LimboServerChooseEvent {
    public AuthenticLimboServerChooseEvent(User user, Audience audience) {
        super(user, audience, user.getUuid());
    }
}
