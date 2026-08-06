/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.ServerChooseEvent;

/**
 * Allows you to determine to which limbo player should be sent.
 *
 * @author kyngs
 * @see ServerChooseEvent#setServer
 */
public interface LimboServerChooseEvent<P, S> extends ServerChooseEvent<P, S> {
}
