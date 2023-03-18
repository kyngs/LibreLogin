/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.PlayerBasedEvent;

/**
 * This event is called after the player has authenticated
 * Note, that this event will both be called if the player has authenticated manually, or automatically.
 * Use {@link #getReason()} to check the reason for authentication.
 *
 * @author kyngs
 */
public interface AuthenticatedEvent<P, S> extends PlayerBasedEvent<P, S> {

    AuthenticationReason getReason();

    enum AuthenticationReason {
        LOGIN,
        REGISTER,
        PREMIUM,
        SESSION
    }

}
