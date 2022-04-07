package xyz.kyngs.librepremium.common.crypto;

import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MessageDigestCryptoProvider implements CryptoProvider {

    private final SecureRandom random;
    private final MessageDigest sha256;
    private final String identifier;

    public MessageDigestCryptoProvider(String identifier) {
        this.identifier = identifier;

        random = new SecureRandom();

        try {
            sha256 = MessageDigest.getInstance(identifier);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String randomSalt() {
        byte[] bytes = new byte[16];
        this.random.nextBytes(bytes);
        return String.format("%016x", new BigInteger(1, bytes));
    }

    private String plainHash(String input) {
        byte[] inputBytes = input.getBytes();
        byte[] hashedBytes = this.sha256.digest(inputBytes);
        return String.format("%064x", new BigInteger(1, hashedBytes));
    }

    @Override
    public HashedPassword createHash(String password) {
        var salt = randomSalt();
        var plain = plainHash(password);
        var hash = plainHash(plain + salt);
        return new HashedPassword(hash, salt, getIdentifier());
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        var salt = password.salt();
        var hash = password.hash();
        var hashedInput = plainHash(plainHash(input) + salt);
        return hashedInput.equals(hash);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
