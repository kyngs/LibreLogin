/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which server player should be sent after authentication, or after being kicked.
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer(S)
 */
public interface LobbyServerChooseEvent<P, S> extends ServerChooseEvent<P, S> {

    /**
     * Checks if the event was called to select a fallback server
     *
     * @return Whether is this event called to select fallback server
     * */
    boolean isFallback();

    /**
     * Abort teleportion of the player
     *
     * @param cancelled If set to true, this event will be aborted
     */
    void setCancelled(boolean cancelled);

    /**
     * Checks if the event is aborted
     *
     * @return Whether is the event aborted
     * */
    boolean isCancelled();

}
