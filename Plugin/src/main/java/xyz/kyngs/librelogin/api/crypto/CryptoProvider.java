package xyz.kyngs.librelogin.api.crypto;

/**
 * This interface manages passwords for its own algorithm. Each algorithm must implement this interface.
 *
 * @author kyngs
 */
public interface CryptoProvider {

    /**
     * Creates a {@link HashedPassword} from a password.
     *
     * @param password The password (in plaintext).
     * @return The hashed password.
     */
    HashedPassword createHash(String password);

    /**
     * Verifies a password against a {@link HashedPassword}.
     *
     * @param input    The password (in plaintext).
     * @param password The hashed password.
     * @return True if the password is correct, false otherwise.
     */
    boolean matches(String input, HashedPassword password);

    /**
     * Gets the name of the algorithm.
     *
     * @return The name of the algorithm.
     */
    String getIdentifier();

}
