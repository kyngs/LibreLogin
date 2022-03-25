package xyz.kyngs.librepremium.api.crypto;

public interface CryptoProvider {

    HashedPassword createHash(String password);

    boolean matches(String input, HashedPassword password);

    String getIdentifier();

}
