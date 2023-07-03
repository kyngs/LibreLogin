/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.premium;

import java.util.UUID;

/**
 * This interface handles {@link PremiumUser} fetching.
 *
 * @author kyngs
 */
public interface PremiumProvider {

    /**
     * This method fetches a user by their username.
     *
     * @param name The username of the user.
     * @return The user, or null if the user does not exist.
     * @throws PremiumException If the user could not be fetched.
     */
    PremiumUser getUserForName(String name) throws PremiumException;

    /**
     * This method fetches a user by their UUID.
     *
     * @param uuid The UUID of the user.
     * @return The user, or null if the user does not exist.
     * @throws PremiumException If the user could not be fetched.
     */
    PremiumUser getUserForUUID(UUID uuid) throws PremiumException;

}
