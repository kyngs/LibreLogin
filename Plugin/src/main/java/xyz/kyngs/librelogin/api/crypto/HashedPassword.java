package xyz.kyngs.librelogin.api.crypto;

import org.jetbrains.annotations.Nullable;

/**
 * This record is used to store a hashed password.
 *
 * @param hash The hash of the password.
 * @param salt The salt of the password.
 * @param algo The algorithm used to hash the password.
 * @author kyngs
 */
public record HashedPassword(String hash, @Nullable String salt, String algo) {
    @Override
    public String toString() {
        return "HashedPassword{" +
                "hash='" + hash + '\'' +
                ", salt='" + salt + '\'' +
                ", algo='" + algo + '\'' +
                '}';
    }
}
