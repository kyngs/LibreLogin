package xyz.kyngs.librepremium.api.event.events;

import xyz.kyngs.librepremium.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which limbo player should be sent.
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer(S)
 */
public interface LimboServerChooseEvent<P, S> extends ServerChooseEvent<P, S> {
}
