/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.CancellableEvent;
import xyz.kyngs.librelogin.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which server player should be sent after authentication, or after being kicked.
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer
 */
public interface LobbyServerChooseEvent<P, S> extends ServerChooseEvent<P, S>, CancellableEvent {

    /**
     * Checks if the event was called to select a fallback server
     *
     * @return Whether is this event called to select fallback server or null if unknown
     * */
    Boolean isFallback();

}
