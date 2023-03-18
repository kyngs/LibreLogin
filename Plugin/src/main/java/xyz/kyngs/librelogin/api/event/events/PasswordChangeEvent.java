/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.api.event.PlayerBasedEvent;

/**
 * Fires after player has changed their password, their profile is already updated at the moment the event fires
 *
 * @author kyngs
 */
public interface PasswordChangeEvent<P, S> extends PlayerBasedEvent<P, S> {

    /**
     * Gets the player's old password, in hashed form
     *
     * @return player's old password
     */
    HashedPassword getOldPassword();

}
