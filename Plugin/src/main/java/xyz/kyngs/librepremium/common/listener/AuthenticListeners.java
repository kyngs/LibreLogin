package xyz.kyngs.librepremium.common.listener;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.premium.PremiumException;
import xyz.kyngs.librepremium.api.premium.PremiumUser;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

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
        if (plugin.getDatabaseProvider().getByUUID(uuid).autoLoginEnabled()) return;
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
                user = checkAndValidateByName(username, true);
            } catch (InvalidCommandArgument e) {
                return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp());
            }

            //noinspection ConstantConditions //kyngs: There's no way IntelliJ is right
            user.setPremiumUUID(null);

            plugin.getDatabaseProvider().saveUser(user);
        } else {
            var user = plugin.getDatabaseProvider().getByPremiumUUID(premium.uuid());

            if (user == null) {
                User userByName;
                try {
                    userByName = checkAndValidateByName(username, true);
                } catch (InvalidCommandArgument e) {
                    return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp());
                }

                plugin.getDatabaseProvider().saveUser(userByName);
            } else {
                User byName;
                try {
                    byName = checkAndValidateByName(username, false);
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

                    plugin.getDatabaseProvider().saveUser(user);
                }

                return new PreLoginResult(PreLoginState.FORCE_ONLINE, null);
            }
        }

        return new PreLoginResult(PreLoginState.FORCE_OFFLINE, null);
    }

    private User checkAndValidateByName(String username, boolean generate) throws InvalidCommandArgument {
        var user = plugin.getDatabaseProvider().getByName(username);

        if (user != null) {
            user.setLastSeen(Timestamp.valueOf(LocalDateTime.now()));
            if (!user.getLastNickname().contentEquals(username)) {
                throw new InvalidCommandArgument(plugin.getMessages().getMessage("kick-invalid-case-username",
                        "%username%", user.getLastNickname()
                ));
            }
        } else if (generate) {
            user = new User(
                    UUID.randomUUID(),
                    null,
                    null,
                    username,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now())
            );
        } else return null;

        return user;
    }

    protected String chooseServer(UUID playerUUID) throws NoSuchElementException {
        if (plugin.getDatabaseProvider().getByUUID(playerUUID).autoLoginEnabled()) {
            return plugin.chooseLobby();
        } else {
            return plugin.getConfiguration().getLimboServer();
        }
    }
}
