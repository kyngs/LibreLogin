package xyz.kyngs.librelogin.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.LimboServerChooseEvent;
import xyz.kyngs.librelogin.common.event.AuthenticServerChooseEvent;

public class AuthenticLimboServerChooseEvent<P, S> extends AuthenticServerChooseEvent<P, S> implements LimboServerChooseEvent<P, S> {
    public AuthenticLimboServerChooseEvent(@Nullable User user, P player, LibreLoginPlugin<P, S> plugin) {
        super(user, player, plugin);
    }
}
