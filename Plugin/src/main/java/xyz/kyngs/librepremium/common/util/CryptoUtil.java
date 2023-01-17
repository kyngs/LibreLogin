package xyz.kyngs.librepremium.common.util;

import java.util.AbstractMap.SimpleEntry;

import xyz.kyngs.librepremium.api.crypto.HashedPassword;

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

    public static SimpleEntry<String, String> convertHash(String hash) {
        var split = hash.split("\\$");

        return new SimpleEntry<>(
                split[0],
                split[1]
        );
    }

    public static String rawFromHashed(HashedPassword password) {
        var extracted = convertHash(password.hash());
        return "$%s$%s$%s%s".formatted(
                password.algo().replace("BCrypt-", "").toLowerCase(),
                extracted.getKey(),
                password.salt(),
                extracted.getValue()
        );
    }

}
