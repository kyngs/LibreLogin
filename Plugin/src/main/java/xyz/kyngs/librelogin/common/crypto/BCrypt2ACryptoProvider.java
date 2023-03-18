/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;
import xyz.kyngs.librelogin.api.crypto.CryptoProvider;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.common.util.CryptoUtil;

public class BCrypt2ACryptoProvider implements CryptoProvider {

    public static final BCrypt.Hasher HASHER = BCrypt
            .with(BCrypt.Version.VERSION_2A);
    public static final BCrypt.Verifyer VERIFIER = BCrypt
            .verifyer(BCrypt.Version.VERSION_2A);

    @Override
    public HashedPassword createHash(String password) {
        return CryptoUtil.convertFromBCryptRaw(
                HASHER.hashToString(10, password.toCharArray())
        );
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        var raw = CryptoUtil.rawFromHashed(password).toCharArray();
        var result = VERIFIER.verify(input.toCharArray(),
                raw
        );

        return result.verified;
    }

    @Override
    public String getIdentifier() {
        return "BCrypt-2A";
    }

}
