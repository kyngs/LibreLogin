package xyz.kyngs.librepremium.api.event.events;

import xyz.kyngs.librepremium.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which limbo player should be sent.
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer(String)
 */
public interface LimboServerChooseEvent extends ServerChooseEvent {
}
