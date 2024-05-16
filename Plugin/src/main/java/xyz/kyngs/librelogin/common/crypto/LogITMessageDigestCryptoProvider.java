/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.crypto;

import xyz.kyngs.librelogin.api.crypto.HashedPassword;

public class LogITMessageDigestCryptoProvider extends MessageDigestCryptoProvider {
    public LogITMessageDigestCryptoProvider(String identifier, String md) {
        super(identifier, md);
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        var salt = password.salt();
        var hash = password.hash();
        var hashedInput = salt == null ? plainHash(input) : plainHash(input + salt);
        return hashedInput.equals(hash);
    }

    @Override
    public HashedPassword createHash(String password) {
        var salt = randomSalt();
        var hash = plainHash(password + salt);
        return new HashedPassword(hash, salt, getIdentifier());
    }
}
