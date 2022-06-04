package xyz.kyngs.librepremium.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.PasswordChangeEvent;
import xyz.kyngs.librepremium.common.event.AuthenticPlayerBasedEvent;

public class AuthenticPasswordChangeEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements PasswordChangeEvent<P, S> {
    private final HashedPassword oldPassword;

    public AuthenticPasswordChangeEvent(@Nullable User user, P player, LibrePremiumPlugin<P, S> plugin, HashedPassword oldPassword) {
        super(user, player, plugin);
        this.oldPassword = oldPassword;
    }

    @Override
    public HashedPassword getOldPassword() {
        return oldPassword;
    }
}
