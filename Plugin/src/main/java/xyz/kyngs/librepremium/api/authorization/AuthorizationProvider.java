package xyz.kyngs.librepremium.api.authorization;

import java.util.UUID;

public interface AuthorizationProvider {

    boolean isAuthorized(UUID uuid);

    /**
     * There is <b>no</b> guarantee, that {@link #isAuthorized(UUID)} for the supplied uuid, returns true.
     *
     * @param uuid
     */
    void authorize(UUID uuid);

}
