package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which limbo player should be sent.
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer(S)
 */
public interface LimboServerChooseEvent<P, S> extends ServerChooseEvent<P, S> {
}
