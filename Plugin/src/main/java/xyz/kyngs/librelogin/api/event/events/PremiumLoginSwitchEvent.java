/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.PlayerBasedEvent;

/**
 * This event is called <b>after</b> the player switches the mode, this means that {@link #getUser()} should already have the new state
 * However, this event is also called <b>before</b> the user gets saved to the database, so you can override the state.
 *
 * @author kyngs
 */
public interface PremiumLoginSwitchEvent<P, S> extends PlayerBasedEvent<P, S> {

}
