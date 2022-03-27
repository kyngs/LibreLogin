package xyz.kyngs.librepremium.common.event;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.ServerChooseEvent;

public class AuthenticServerChooseEvent extends AuthenticPlayerBasedEvent implements ServerChooseEvent {

    private String server = null;

    public AuthenticServerChooseEvent(User user, Audience audience) {
        super(user, audience);
    }

    @Nullable
    @Override
    public String getServer() {
        return server;
    }

    @Override
    public void setServer(String server) {
        this.server = server;
    }
}
