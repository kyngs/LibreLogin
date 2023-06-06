/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.crypto;

import javax.annotation.Nullable;

/**
 * This interface manages passwords for its own algorithm. Each algorithm must implement this interface.
 *
 * @author kyngs
 */
public interface CryptoProvider {

    /**
     * Creates a {@link HashedPassword} from a password.
     *
     * @param password The password (in plaintext).
     * @return Hashed password or null if hashing failed (e.g. password is too long).
     */
    @Nullable
    HashedPassword createHash(String password);

    /**
     * Verifies a password against a {@link HashedPassword}.
     *
     * @param input    The password (in plaintext).
     * @param password The hashed password.
     * @return True if the password is correct, false otherwise.
     */
    boolean matches(String input, HashedPassword password);

    /**
     * Gets the name of the algorithm.
     *
     * @return The name of the algorithm.
     */
    String getIdentifier();

}
