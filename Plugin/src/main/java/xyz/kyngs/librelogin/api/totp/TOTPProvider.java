/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.totp;

import xyz.kyngs.librelogin.api.database.User;

/**
 * This interface handles TOTP data creation and verification.
 *
 * @author kyngs
 */
public interface TOTPProvider {

    /**
     * Generates new TOTP data for a user.
     *
     * @param user The user to generate TOTP data for.
     * @return The TOTP data.
     */
    TOTPData generate(User user);

    /**
     * Generates a new TOTP data for a user.
     *
     * @param user   The user to generate TOTP data for.
     * @param secret The secret to use for the TOTP.
     * @return The TOTP data.
     */
    TOTPData generate(User user, String secret);

    /**
     * Verifies a TOTP code for a user.
     *
     * @param code   The code to verify.
     * @param secret The secret to use for the TOTP.
     * @return Whether the code was correct.
     */
    boolean verify(Integer code, String secret);

}
