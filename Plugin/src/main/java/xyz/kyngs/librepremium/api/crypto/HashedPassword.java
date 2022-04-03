package xyz.kyngs.librepremium.api.crypto;

import org.jetbrains.annotations.Nullable;

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
