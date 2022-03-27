package xyz.kyngs.librepremium.common.event.events;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.PasswordChangeEvent;
import xyz.kyngs.librepremium.common.event.AuthenticPlayerBasedEvent;

public class AuthenticPasswordChangeEvent extends AuthenticPlayerBasedEvent implements PasswordChangeEvent {
    private final HashedPassword oldPassword;

    public AuthenticPasswordChangeEvent(User user, Audience audience, HashedPassword oldPassword) {
        super(user, audience);
        this.oldPassword = oldPassword;
    }

    @Override
    public HashedPassword getOldPassword() {
        return oldPassword;
    }
}
