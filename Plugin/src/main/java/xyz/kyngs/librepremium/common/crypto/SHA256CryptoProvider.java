package xyz.kyngs.librepremium.common.crypto;

import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SHA256CryptoProvider implements CryptoProvider {

    private final SecureRandom random;

    private final MessageDigest sha256;

    public SHA256CryptoProvider() {
        try {
            this.random = new SecureRandom();
            this.sha256 = MessageDigest.getInstance(getIdentifier());
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
        String salt = randomSalt();
        String plain = plainHash(password);
        String hash = plainHash(plain + salt);
        return new HashedPassword(hash, salt, getIdentifier());
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        String salt = password.salt();
        String hash = password.hash();
        String hashedInput = plainHash(plainHash(input) + salt);
        return hashedInput.equals(hash);
    }

    @Override
    public String getIdentifier() {
        return "SHA-256";
    }

}
