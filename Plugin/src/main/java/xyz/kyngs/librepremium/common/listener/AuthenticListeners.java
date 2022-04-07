package xyz.kyngs.librepremium.common.listener;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librepremium.api.premium.PremiumException;
import xyz.kyngs.librepremium.api.premium.PremiumUser;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;
import xyz.kyngs.librepremium.common.event.events.AuthenticAuthenticatedEvent;
import xyz.kyngs.librepremium.common.event.events.AuthenticPremiumLoginSwitchEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthenticListeners<P extends AuthenticLibrePremium> {

    @SuppressWarnings("RegExpSimplifiable") //I don't believe you
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");

    protected final P plugin;

    public AuthenticListeners(P plugin) {
        this.plugin = plugin;
    }

    protected void onPostLogin(UUID uuid, Audience audience) {
        if (plugin.getDatabaseProvider().getByUUID(uuid).autoLoginEnabled()) {
            plugin.getEventProvider().fire(AuthenticatedEvent.class, new AuthenticAuthenticatedEvent(plugin.getDatabaseProvider().getByUUID(uuid), audience));
            return;
        }
        plugin.getAuthorizationProvider().startTracking(uuid, audience);
    }
    protected void onPlayerDisconnect(UUID uuid) {
        plugin.getAuthorizationProvider().stopTracking(uuid);
    }

    protected PreLoginResult onPreLogin(String username) {
        if (username.length() > 16 || !NAME_PATTERN.matcher(username).matches()) {
            return new PreLoginResult(PreLoginState.DENIED, plugin.getMessages().getMessage("kick-illegal-username"));
        }

        PremiumUser premium;

        try {
            premium = plugin.getPremiumProvider().getUserForName(username);
        } catch (PremiumException e) {
            var message = switch (e.getIssue()) {
                case THROTTLED -> plugin.getMessages().getMessage("premium-error-throttled-kick");
                default -> {
                    plugin.getLogger().error("Encountered an exception while communicating with the mojang API!");
                    e.printStackTrace();
                    yield plugin.getMessages().getMessage("premium-error-undefined-kick");
                }
            };

            return new PreLoginResult(PreLoginState.DENIED, message);
        }

        if (premium == null) {
            User user;
            try {
                user = checkAndValidateByName(username, null, true);
            } catch (InvalidCommandArgument e) {
                return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp());
            }

            //noinspection ConstantConditions //kyngs: There's no way IntelliJ is right
            if (user.getPremiumUUID() != null) {
                user.setPremiumUUID(null);
                plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent(user, Audience.empty()));
            }

            plugin.getDatabaseProvider().updateUser(user);
        } else {
            var premiumID = premium.uuid();
            var user = plugin.getDatabaseProvider().getByPremiumUUID(premiumID);

            if (user == null) {
                User userByName;
                try {
                    userByName = checkAndValidateByName(username, premiumID, true);
                } catch (InvalidCommandArgument e) {
                    return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp());
                }

                plugin.getDatabaseProvider().updateUser(userByName);
            } else {
                User byName;
                try {
                    byName = checkAndValidateByName(username, premiumID, false);
                } catch (InvalidCommandArgument e) {
                    return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp());
                }

                if (byName != null && user != byName) {
                    // Oh, no
                    return new PreLoginResult(PreLoginState.DENIED, plugin.getMessages().getMessage("kick-name-mismatch",
                            "%nickname%", username
                    ));
                }

                if (!user.getLastNickname().contentEquals(premium.name())) {
                    user.setLastNickname(premium.name());

                    plugin.getDatabaseProvider().updateUser(user);
                }

                return new PreLoginResult(PreLoginState.FORCE_ONLINE, null);
            }
        }

        return new PreLoginResult(PreLoginState.FORCE_OFFLINE, null);
    }

    private User checkAndValidateByName(String username, @Nullable UUID premiumID, boolean generate) throws InvalidCommandArgument {
        var user = plugin.getDatabaseProvider().getByName(username);

        if (user != null) {
            user.setLastSeen(Timestamp.valueOf(LocalDateTime.now()));
            if (!user.getLastNickname().contentEquals(username)) {
                throw new InvalidCommandArgument(plugin.getMessages().getMessage("kick-invalid-case-username",
                        "%username%", user.getLastNickname()
                ));
            }
        } else if (generate) {
            var newID = plugin.generateNewUUID(
                    username,
                    premiumID
            );

            var conflictingUser = plugin.getDatabaseProvider().getByUUID(newID);

            if (conflictingUser != null) {
                throw new InvalidCommandArgument(plugin.getMessages().getMessage("kick-occupied-username",
                        "%username%", conflictingUser.getLastNickname()
                ));
            }

            user = new User(
                    newID,
                    null,
                    null,
                    username,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now())
            );

            plugin.getDatabaseProvider().insertUser(user);
        } else return null;

        return user;
    }

    protected String chooseServer(UUID playerUUID, Audience audience) throws NoSuchElementException {
        var user = plugin.getDatabaseProvider().getByUUID(playerUUID);
        if (plugin.getDatabaseProvider().getByUUID(playerUUID).autoLoginEnabled()) {
            return plugin.chooseLobby(user, audience);
        } else {
            return plugin.getLimboServer(audience, user);
        }
    }
}
