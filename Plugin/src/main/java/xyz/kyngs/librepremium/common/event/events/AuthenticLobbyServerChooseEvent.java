package xyz.kyngs.librepremium.common.event.events;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.LobbyServerChooseEvent;
import xyz.kyngs.librepremium.common.event.AuthenticServerChooseEvent;

public class AuthenticLobbyServerChooseEvent extends AuthenticServerChooseEvent implements LobbyServerChooseEvent {
    public AuthenticLobbyServerChooseEvent(User user, Audience audience) {
        super(user, audience);
    }
}
