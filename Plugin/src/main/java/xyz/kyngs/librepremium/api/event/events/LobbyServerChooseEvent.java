package xyz.kyngs.librepremium.api.event.events;

import xyz.kyngs.librepremium.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which server player should be sent after authentication
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer(S)
 */
public interface LobbyServerChooseEvent<P, S> extends ServerChooseEvent<P, S> {
}
