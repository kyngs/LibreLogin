package xyz.kyngs.librepremium.api.authorization;

import xyz.kyngs.librepremium.api.database.User;

public interface AuthorizationProvider<P> {

    boolean isAuthorized(P player);

    boolean isAwaiting2FA(P player);

    void authorize(User user, P player);
}
