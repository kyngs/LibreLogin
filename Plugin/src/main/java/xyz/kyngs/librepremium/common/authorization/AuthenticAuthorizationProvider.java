package xyz.kyngs.librepremium.common.authorization;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.title.Title;
import xyz.kyngs.librepremium.api.authorization.AuthorizationProvider;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.event.events.AuthenticAuthenticatedEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticAuthorizationProvider implements AuthorizationProvider {

    private final Map<UUID, Boolean> unAuthorized;
    private final AuthenticLibrePremium plugin;

    public AuthenticAuthorizationProvider(AuthenticLibrePremium plugin) {
        this.plugin = plugin;
        unAuthorized = new HashMap<>();
    }

    @Override
    public boolean isAuthorized(UUID uuid) {
        return !unAuthorized.containsKey(uuid);
    }

    @Override
    public void authorize(User user, Audience audience) {
        var uuid = user.getUuid();
        stopTracking(uuid);

        audience.clearTitle();
        plugin.getEventProvider().fire(AuthenticatedEvent.class, new AuthenticAuthenticatedEvent(user, audience));
        plugin.authorize(uuid, user, audience);
    }

    public void startTracking(User user, Audience audience) {
        unAuthorized.put(user.getUuid(), user.isRegistered());

        plugin.cancelOnExit(plugin.delay(() -> {
            if (!unAuthorized.containsKey(user.getUuid())) return;
            sendInfoMessage(user.isRegistered(), audience);
        }, 250), user.getUuid());

        var limit = plugin.getConfiguration().secondsToAuthorize();

        if (limit > 0) {
            plugin.cancelOnExit(plugin.delay(() -> {
                if (!unAuthorized.containsKey(user.getUuid())) return;
                plugin.kick(user.getUuid(), plugin.getMessages().getMessage("kick-time-limit"));
            }, limit * 1000L), user.getUuid());
        }


    }

    private void sendInfoMessage(boolean registered, Audience audience) {
        audience.sendMessage(plugin.getMessages().getMessage(registered ? "prompt-login" : "prompt-register"));
        if (!plugin.getConfiguration().useTitles()) return;
        var toRefresh = plugin.getConfiguration().milliSecondsToRefreshNotification();
        //noinspection UnstableApiUsage
        audience.showTitle(Title.title(
                plugin.getMessages().getMessage(registered ? "title-login" : "title-register"),
                plugin.getMessages().getMessage(registered ? "sub-title-login" : "sub-title-register"),
                Title.Times.of(
                        Duration.ofMillis(0),
                        Duration.ofMillis(toRefresh > 0 ?
                                (long) (toRefresh * 1.1) :
                                10000
                        ),
                        Duration.ofMillis(0)
                )
        ));
    }

    public void stopTracking(UUID uuid) {
        unAuthorized.remove(uuid);
    }

    public void notifyUnauthorized() {
        unAuthorized.forEach((uuid, registered) -> sendInfoMessage(registered, plugin.getAudienceForID(uuid)));
    }
}
