/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.crypto.CryptoProvider;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.common.util.CryptoUtil;

import java.security.SecureRandom;
import java.util.Arrays;

public class Argon2IDCryptoProvider implements CryptoProvider {

    private final Logger logger;
    private SecureRandom random;

    public Argon2IDCryptoProvider(Logger logger) {
        this.logger = logger;
        random = new SecureRandom();
    }

    @Nullable
    @Override
    public HashedPassword createHash(String password) {
        var start = System.currentTimeMillis();
        var salt = new byte[16];
        random.nextBytes(salt);

        var hash = new byte[32];

        var params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(1 << 14)
                .withIterations(2)
                .build();

        var generator = new Argon2BytesGenerator();
        generator.init(params);
        generator.generateBytes(password.toCharArray(), hash);

        logger.debug("Argon2ID hash took " + (System.currentTimeMillis() - start) + "ms");

        return CryptoUtil.convertFromArgon2ID(hash, params);
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        var params = CryptoUtil.rawArgonFromHashed(password);

        var hashBytes = new byte[params.hash().length];
        var generator = new Argon2BytesGenerator();
        generator.init(params.parameters());
        generator.generateBytes(input.toCharArray(), hashBytes);

        return Arrays.equals(hashBytes, params.hash());
    }

    @Override
    public String getIdentifier() {
        return "Argon-2ID";
    }
}
