/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.PlayerBasedEvent;

/**
 * This event is called after the player has tried to enter the password, but it doesn't match with the player's real password.
 * Note, that this event is fired when the player executes the following commands: /login, /changepassword, /premium, /setemail.
 * Use {@link #getSource()} to check the source of the event.
 *
 */
public interface WrongPasswordEvent<P, S> extends PlayerBasedEvent<P, S> {

    /**
     * Returns the source of the incorrect password.
     *
     * @return The source of the incorrect password
     */
    AuthenticationSource getSource();

    /**
     * Possible sources for incorrect password.
     */
    enum AuthenticationSource {
        /**
         * The player has used the /login command with a wrong password
         */
        LOGIN,
        /**
         * The player has used the /login command with a wrong TOTP code
         */
        TOTP,
        /**
         * The player has used the /changepassword command with a wrong password
         */
        CHANGE_PASSWORD,
        /**
         * The player has used the /premium command with a wrong password
         */
        PREMIUM_ENABLE,
        /**
         * The player has used the /setemail command with a wrong password
         */
        SET_EMAIL
    }
}
