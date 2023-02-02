package xyz.kyngs.librelogin.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibrePremiumPlugin;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.LobbyServerChooseEvent;
import xyz.kyngs.librelogin.common.event.AuthenticServerChooseEvent;

public class AuthenticLobbyServerChooseEvent<P, S> extends AuthenticServerChooseEvent<P, S> implements LobbyServerChooseEvent<P, S> {
    public AuthenticLobbyServerChooseEvent(@Nullable User user, P player, LibrePremiumPlugin<P, S> plugin) {
        super(user, player, plugin);
    }
}
