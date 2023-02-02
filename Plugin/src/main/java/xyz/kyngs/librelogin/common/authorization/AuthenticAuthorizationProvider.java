package xyz.kyngs.librelogin.common.authorization;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.title.Title;
import xyz.kyngs.librelogin.api.authorization.AuthorizationProvider;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.api.totp.TOTPData;
import xyz.kyngs.librelogin.common.AuthenticHandler;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.event.events.AuthenticAuthenticatedEvent;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AuthenticAuthorizationProvider<P, S> extends AuthenticHandler<P, S> implements AuthorizationProvider<P> {

    private final Map<P, Boolean> unAuthorized;
    private final Map<P, String> awaiting2FA;

    public AuthenticAuthorizationProvider(AuthenticLibreLogin<P, S> plugin) {
        super(plugin);
        unAuthorized = new HashMap<>();
        awaiting2FA = new HashMap<>();
    }

    @Override
    public boolean isAuthorized(P player) {
        return !unAuthorized.containsKey(player);
    }

    @Override
    public boolean isAwaiting2FA(P player) {
        return awaiting2FA.containsKey(player);
    }

    @Override
    public void authorize(User user, P player, AuthenticatedEvent.AuthenticationReason reason) {
        if (isAuthorized(player)) {
            throw new IllegalStateException("Player is already authorized");
        }
        stopTracking(player);

        user.setLastAuthentication(Timestamp.valueOf(LocalDateTime.now()));
        user.setIp(platformHandle.getIP(player));
        plugin.getDatabaseProvider().updateUser(user);

        var audience = platformHandle.getAudienceForPlayer(player);

        audience.clearTitle();
        plugin.getEventProvider().fire(AuthenticatedEvent.class, new AuthenticAuthenticatedEvent<>(user, player, plugin, reason));
        plugin.authorize(player, user, audience);
    }

    @Override
    public boolean confirmTwoFactorAuth(P player, Integer code, User user) {
        var secret = awaiting2FA.get(player);
        if (plugin.getTOTPProvider().verify(code, secret)) {
            user.setSecret(secret);
            plugin.getDatabaseProvider().updateUser(user);
            return true;
        }
        return false;
    }

    public void startTracking(User user, P player) {
        var audience = platformHandle.getAudienceForPlayer(player);

        unAuthorized.put(player, user.isRegistered());

        plugin.cancelOnExit(plugin.delay(() -> {
            if (!unAuthorized.containsKey(player)) return;
            sendInfoMessage(user.isRegistered(), audience);
        }, 250), player);

        var limit = plugin.getConfiguration().secondsToAuthorize();

        if (limit > 0) {
            plugin.cancelOnExit(plugin.delay(() -> {
                if (!unAuthorized.containsKey(player)) return;
                platformHandle.kick(player, plugin.getMessages().getMessage("kick-time-limit"));
            }, limit * 1000L), player);
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

    public void stopTracking(P player) {
        unAuthorized.remove(player);
    }

    public void notifyUnauthorized() {
        var wrong = new HashSet<P>();
        unAuthorized.forEach((player, registered) -> {
            var audience = platformHandle.getAudienceForPlayer(player);

            if (audience == null) {
                wrong.add(player);
                return;
            }

            sendInfoMessage(registered, audience);

        });

        wrong.forEach(unAuthorized::remove);
    }

    public void onExit(P player) {
        stopTracking(player);
        awaiting2FA.remove(player);
    }

    public void beginTwoFactorAuth(User user, P player, TOTPData data) {
        awaiting2FA.put(player, data.secret());

        platformHandle.movePlayer(player, plugin.chooseLimbo(user, player)).whenComplete((t, e) -> {
            if (t != null || e != null) awaiting2FA.remove(player);
        });
    }
}
