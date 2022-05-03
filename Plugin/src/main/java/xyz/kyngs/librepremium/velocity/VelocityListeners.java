package xyz.kyngs.librepremium.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.util.GameProfile;
import xyz.kyngs.librepremium.common.listener.AuthenticListeners;

import java.util.NoSuchElementException;

public class VelocityListeners extends AuthenticListeners<VelocityLibrePremium> {
    public VelocityListeners(VelocityLibrePremium plugin) {
        super(plugin);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        onPostLogin(event.getPlayer().getUniqueId(), event.getPlayer());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        onPlayerDisconnect(event.getPlayer().getUniqueId());
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onProfileRequest(GameProfileRequestEvent event) {
        var existing = event.getGameProfile();

        if (existing != null && plugin.fromFloodgate(existing.getId())) return;

        var profile = plugin.getDatabaseProvider().getByName(event.getUsername());

        var gProfile = event.getOriginalProfile();

        event.setGameProfile(new GameProfile(profile.getUuid(), gProfile.getName(), gProfile.getProperties()));
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event) {

        if (!event.getResult().isAllowed() || event.getResult() == PreLoginEvent.PreLoginComponentResult.forceOfflineMode())
            return; // The offline mode checking thingy indicates that the player is coming from Floodgate. I know this is a terrible solution, but it's the best I have for now.

        var result = onPreLogin(event.getUsername());

        event.setResult(
                switch (result.state()) {
                    case DENIED -> {
                        assert result.message() != null;
                        yield PreLoginEvent.PreLoginComponentResult.denied(result.message());
                    }
                    case FORCE_ONLINE -> PreLoginEvent.PreLoginComponentResult.forceOnlineMode();
                    case FORCE_OFFLINE -> PreLoginEvent.PreLoginComponentResult.forceOfflineMode();
                }
        );

    }

    @Subscribe(order = PostOrder.LAST)
    public void chooseServer(PlayerChooseInitialServerEvent event) {
        try {
            event.setInitialServer(plugin.getServer()
                    .getServer(
                            chooseServer(event.getPlayer().getUniqueId(), event.getPlayer())
                    )
                    .orElseThrow()
            );

        } catch (NoSuchElementException e) {
            event.getPlayer().disconnect(plugin.getMessages().getMessage("kick-no-server"));
        }

    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!plugin.getAuthorizationProvider().isAuthorized(event.getPlayer().getUniqueId())) {
            event.getResult().getServer().ifPresentOrElse(registeredServer -> {
                if (!plugin.getConfiguration().getLimbo().contains(registeredServer.getServerInfo().getName())) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                }
            }, () -> event.setResult(ServerPreConnectEvent.ServerResult.denied()));
        }
    }

}
