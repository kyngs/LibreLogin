package xyz.kyngs.librepremium.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.common.event.AuthenticPlayerBasedEvent;

public class AuthenticAuthenticatedEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements AuthenticatedEvent<P, S> {
    public AuthenticAuthenticatedEvent(@Nullable User user, P player, LibrePremiumPlugin<P, S> plugin) {
        super(user, player, plugin);
    }
}
