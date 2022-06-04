package xyz.kyngs.librepremium.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.LimboServerChooseEvent;
import xyz.kyngs.librepremium.common.event.AuthenticServerChooseEvent;

public class AuthenticLimboServerChooseEvent<P, S> extends AuthenticServerChooseEvent<P, S> implements LimboServerChooseEvent<P, S> {
    public AuthenticLimboServerChooseEvent(@Nullable User user, P player, LibrePremiumPlugin<P, S> plugin) {
        super(user, player, plugin);
    }
}
