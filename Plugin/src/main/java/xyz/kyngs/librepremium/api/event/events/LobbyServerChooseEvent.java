package xyz.kyngs.librepremium.api.event.events;

import xyz.kyngs.librepremium.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which server player should be sent after authentication
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer(String)
 */
public interface LobbyServerChooseEvent extends ServerChooseEvent {
}
