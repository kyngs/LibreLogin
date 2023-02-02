package xyz.kyngs.librelogin.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibrePremiumPlugin;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librelogin.common.event.AuthenticPlayerBasedEvent;

public class AuthenticPremiumLoginSwitchEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements PremiumLoginSwitchEvent<P, S> {
    public AuthenticPremiumLoginSwitchEvent(@Nullable User user, P player, LibrePremiumPlugin<P, S> plugin) {
        super(user, player, plugin);
    }
}
