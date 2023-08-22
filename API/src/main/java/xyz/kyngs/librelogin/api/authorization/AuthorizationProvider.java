/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.authorization;

import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;

/**
 * This interface manages user authorization.
 *
 * @param <P> The type of the player.
 * @author kyngs
 */
public interface AuthorizationProvider<P> {

    /**
     * Checks whether the user has already passed the login process.
     *
     * @param player The player.
     * @return True if the user has already passed the login process, false otherwise.
     */
    boolean isAuthorized(P player);

    /**
     * Checks whether the player is in the process of enabling 2FA.
     *
     * @param player The player.
     * @return True if the player is in the process of enabling 2FA, false otherwise.
     */
    boolean isAwaiting2FA(P player);

    /**
     * Authorizes the player, if the player is not already authorized. Implementation must make sure that {@link #isAuthorized(P)} returns false.
     *
     * @param user   The user.
     * @param player The player.
     * @param reason The reason for authorization.
     */
    void authorize(User user, P player, AuthenticatedEvent.AuthenticationReason reason);

    /**
     * Finishes the 2FA enabling process.
     *
     * @param player The player.
     * @param code   The code.
     * @param user   The user.
     * @return whether the code is valid.
     */
    boolean confirmTwoFactorAuth(P player, Integer code, User user);
}
