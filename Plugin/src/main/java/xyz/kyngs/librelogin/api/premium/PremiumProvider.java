package xyz.kyngs.librelogin.api.premium;

/**
 * This interface handles {@link PremiumUser} fetching.
 *
 * @author kyngs
 */
public interface PremiumProvider {

    /**
     * This method fetches a user by their username.
     *
     * @param name The username of the user.
     * @return The user, or null if the user does not exist.
     * @throws PremiumException If the user could not be fetched.
     */
    PremiumUser getUserForName(String name) throws PremiumException;

}
