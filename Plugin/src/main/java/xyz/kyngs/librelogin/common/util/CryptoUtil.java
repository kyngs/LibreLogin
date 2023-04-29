/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.util;

import org.bouncycastle.crypto.params.Argon2Parameters;
import xyz.kyngs.librelogin.api.BiHolder;
import xyz.kyngs.librelogin.api.crypto.HashedPassword;

import java.util.Base64;

public class CryptoUtil {

    public static HashedPassword convertFromBCryptRaw(String raw) {
        var split = raw.split("\\$");
        var algo = split[1];
        var cost = split[2];
        var rest = split[3];

        var salt = rest.substring(0, 22);
        var hash = rest.substring(22);

        algo = "BCrypt-" + algo.toUpperCase();

        return new HashedPassword(
                cost + "$" + hash,
                salt,
                algo
        );
    }

    public static BiHolder<String, String> convertHash(String hash) {
        var split = hash.split("\\$");

        return new BiHolder<>(
                split[0],
                split[1]
        );
    }

    public static String rawBcryptFromHashed(HashedPassword password) {
        var extracted = convertHash(password.hash());
        return "$%s$%s$%s%s".formatted(
                password.algo().replace("BCrypt-", "").toLowerCase(),
                extracted.key(),
                password.salt(),
                extracted.value()
        );
    }

    public static HashedPassword convertFromArgon2ID(byte[] hash, Argon2Parameters parameters) {
        return new HashedPassword(
                parameters.getVersion() + "," + parameters.getIterations() + "," + parameters.getMemory() + "$" + Base64.getEncoder().encodeToString(hash),
                Base64.getEncoder().encodeToString(parameters.getSalt()),
                "Argon-2ID"
        );
    }

    public static Argon2IDHashedPassword rawArgonFromHashed(HashedPassword password) {
        var extracted = convertHash(password.hash());

        var split = extracted.key().split(",");

        var version = Integer.parseInt(split[0]);
        var iterations = Integer.parseInt(split[1]);
        var memory = Integer.parseInt(split[2]);

        var salt = Base64.getDecoder().decode(password.salt());
        var hash = Base64.getDecoder().decode(extracted.value());

        return new Argon2IDHashedPassword(hash, new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(version)
                .withIterations(iterations)
                .withSalt(salt)
                .withMemoryAsKB(memory)
                .build());
    }

    public record Argon2IDHashedPassword(byte[] hash, Argon2Parameters parameters) {
    }

}
