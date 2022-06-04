package xyz.kyngs.librepremium.common.event;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.ServerChooseEvent;

public class AuthenticServerChooseEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements ServerChooseEvent<P, S> {

    private S server = null;

    public AuthenticServerChooseEvent(@Nullable User user, P player, LibrePremiumPlugin<P, S> plugin) {
        super(user, player, plugin);
    }

    @Nullable
    @Override
    public S getServer() {
        return server;
    }

    @Override
    public void setServer(S server) {
        this.server = server;
    }
}
