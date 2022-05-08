package xyz.kyngs.librepremium.api.authorization;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;

import java.util.UUID;

public interface AuthorizationProvider {

    boolean isAuthorized(UUID uuid);

    boolean isAwaiting2FA(UUID uuid);

    /**
     * There is <b>no</b> guarantee, that {@link #isAuthorized(UUID)} for the supplied user, returns true.
     */
    void authorize(User user, Audience audience);
}
