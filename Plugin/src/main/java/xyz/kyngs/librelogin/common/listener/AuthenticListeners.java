/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.listener;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.BiHolder;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.api.premium.PremiumException;
import xyz.kyngs.librelogin.api.premium.PremiumUser;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.database.AuthenticUser;
import xyz.kyngs.librelogin.common.event.events.AuthenticAuthenticatedEvent;
import xyz.kyngs.librelogin.common.event.events.AuthenticPremiumLoginSwitchEvent;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthenticListeners<Plugin extends AuthenticLibreLogin<P, S>, P, S> {

    @SuppressWarnings("RegExpSimplifiable") //I don't believe you
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");

    protected final Plugin plugin;
    protected final PlatformHandle<P, S> platformHandle;

    public AuthenticListeners(Plugin plugin) {
        this.plugin = plugin;
        platformHandle = plugin.getPlatformHandle();
    }

    protected void onPostLogin(P player, User user) {
        var ip = platformHandle.getIP(player);
        var uuid = platformHandle.getUUIDForPlayer(player);
        if (plugin.fromFloodgate(uuid)) return;

        if (user == null) {
            user = plugin.getDatabaseProvider().getByUUID(uuid);
        }
        var sessionTime = Duration.ofSeconds(plugin.getConfiguration().get(ConfigurationKeys.SESSION_TIMEOUT));

        if (user.autoLoginEnabled()) {
            plugin.delay(() -> plugin.getPlatformHandle().getAudienceForPlayer(player).sendMessage(plugin.getMessages().getMessage("info-premium-logged-in")), 500);
            plugin.getEventProvider().fire(plugin.getEventTypes().authenticated, new AuthenticAuthenticatedEvent<>(user, player, plugin, AuthenticatedEvent.AuthenticationReason.PREMIUM));
        } else if (sessionTime != null && user.getLastAuthentication() != null && ip.equals(user.getIp()) && user.getLastAuthentication().toLocalDateTime().plus(sessionTime).isAfter(LocalDateTime.now())) {
            plugin.delay(() -> plugin.getPlatformHandle().getAudienceForPlayer(player).sendMessage(plugin.getMessages().getMessage("info-session-logged-in")), 500);
            plugin.getEventProvider().fire(plugin.getEventTypes().authenticated, new AuthenticAuthenticatedEvent<>(user, player, plugin, AuthenticatedEvent.AuthenticationReason.SESSION));
        } else {
            plugin.getAuthorizationProvider().startTracking(user, player);
        }

        user.setLastSeen(Timestamp.valueOf(LocalDateTime.now()));

        var finalUser = user;
        plugin.delay(() -> plugin.getDatabaseProvider().updateUser(finalUser), 0);

    }

    protected void onPlayerDisconnect(P player) {
        plugin.onExit(player);
        plugin.getAuthorizationProvider().onExit(player);
    }

    protected PreLoginResult onPreLogin(String username, InetAddress address) {
        if (username.length() > 16 || !NAME_PATTERN.matcher(username).matches()) {
            return new PreLoginResult(PreLoginState.DENIED, plugin.getMessages().getMessage("kick-illegal-username"), null);
        }

        PremiumUser premium;

        try {
            premium = plugin.getPremiumProvider().getUserForName(username);
        } catch (PremiumException e) {
            var message = switch (e.getIssue()) {
                case THROTTLED -> plugin.getMessages().getMessage("kick-premium-error-throttled");
                default -> {
                    plugin.getLogger().error("Encountered an exception while communicating with the Mojang API!");
                    e.printStackTrace();
                    yield plugin.getMessages().getMessage("kick-premium-error-undefined");
                }
            };

            return new PreLoginResult(PreLoginState.DENIED, message, null);
        }

        if (premium == null) {
            User user;
            try {
                user = checkAndValidateByName(username, null, true, address);
            } catch (InvalidCommandArgument e) {
                return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp(), null);
            }

            //noinspection ConstantConditions //kyngs: There's no way IntelliJ is right
            if (user.getPremiumUUID() != null) {
                user.setPremiumUUID(null);
                plugin.getDatabaseProvider().updateUser(user);
                plugin.getEventProvider().fire(plugin.getEventTypes().premiumLoginSwitch, new AuthenticPremiumLoginSwitchEvent<>(user, null, plugin));
            }
        } else {
            var premiumID = premium.uuid();
            var user = plugin.getDatabaseProvider().getByPremiumUUID(premiumID);

            if (user == null) {
                User userByName;
                try {
                    userByName = checkAndValidateByName(username, premiumID, true, address);
                } catch (InvalidCommandArgument e) {
                    return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp(), null);
                }

                //noinspection ConstantConditions //kyngs: There's no way IntelliJ is right
                if (userByName.autoLoginEnabled())
                    return new PreLoginResult(PreLoginState.FORCE_ONLINE, null, userByName);
            } else {
                User byName;
                try {
                    byName = checkAndValidateByName(username, premiumID, false, address);
                } catch (InvalidCommandArgument e) {
                    return new PreLoginResult(PreLoginState.DENIED, e.getUserFuckUp(), null);
                }

                if (byName != null && !user.equals(byName)) {
                    // Oh, no
                    return new PreLoginResult(PreLoginState.DENIED, plugin.getMessages().getMessage("kick-name-mismatch",
                            "%nickname%", username
                    ), null);
                }

                if (!user.getLastNickname().contentEquals(premium.name())) {
                    user.setLastNickname(premium.name());

                    plugin.getDatabaseProvider().updateUser(user);
                }

                return new PreLoginResult(PreLoginState.FORCE_ONLINE, null, user);
            }
        }

        return new PreLoginResult(PreLoginState.FORCE_OFFLINE, null, null);
    }

    private User checkAndValidateByName(String username, @Nullable UUID premiumID, boolean generate, InetAddress ip) throws InvalidCommandArgument {
        var user = plugin.getDatabaseProvider().getByName(username);

        if (user != null) {
            if (!user.getLastNickname().contentEquals(username)) {
                throw new InvalidCommandArgument(plugin.getMessages().getMessage("kick-invalid-case-username",
                        "%username%", user.getLastNickname()
                ));
            }
        } else if (generate) {
            var minLength = plugin.getConfiguration().get(ConfigurationKeys.MINIMUM_USERNAME_LENGTH);
            if (username.length() < minLength) {
                throw new InvalidCommandArgument(plugin.getMessages().getMessage("kick-short-username",
                        "%length%", String.valueOf(minLength)
                ));
            }

            var ipLimit = plugin.getConfiguration().get(ConfigurationKeys.IP_LIMIT);
            if (ipLimit > 0) {
                var ipCount = plugin.getDatabaseProvider().getByIP(ip.getHostAddress()).size(); // Ideally, this should be a count query, but I'm too lazy to implement that and the performance impact is negligible.

                if (ipCount >= ipLimit) {
                    throw new InvalidCommandArgument(plugin.getMessages().getMessage("kick-ip-limit",
                            "%limit%", String.valueOf(ipLimit)
                    ));
                }
            }

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

            if (premiumID != null && plugin.getConfiguration().get(ConfigurationKeys.AUTO_REGISTER)) {
                user = new AuthenticUser(
                        newID,
                        premiumID,
                        null,
                        username,
                        Timestamp.valueOf(LocalDateTime.now()),
                        Timestamp.valueOf(LocalDateTime.now()),
                        null,
                        ip.getHostAddress(),
                        null,
                        null);
            } else {
                user = new AuthenticUser(
                        newID,
                        null,
                        null,
                        username,
                        Timestamp.valueOf(LocalDateTime.now()),
                        Timestamp.valueOf(LocalDateTime.now()),
                        null,
                        ip.getHostAddress(),
                        null,
                        null);
            }

            plugin.getDatabaseProvider().insertUser(user);
        } else return null;

        return user;
    }

    protected BiHolder<Boolean, S> chooseServer(P player, @Nullable String ip, @Nullable User user) {
        var id = platformHandle.getUUIDForPlayer(player);
        var fromFloodgate = plugin.fromFloodgate(id);

        var sessionTime = Duration.ofSeconds(plugin.getConfiguration().get(ConfigurationKeys.SESSION_TIMEOUT));

        if (fromFloodgate) {
            user = null;
        } else if (user == null) {
            user = plugin.getDatabaseProvider().getByUUID(id);
        }

        if (ip == null) {
            ip = platformHandle.getIP(player);
        }

        if (fromFloodgate || user.autoLoginEnabled() || (sessionTime != null && user.getLastAuthentication() != null && ip.equals(user.getIp()) && user.getLastAuthentication().toLocalDateTime().plus(sessionTime).isAfter(LocalDateTime.now()))) {
            return new BiHolder<>(true, plugin.getServerHandler().chooseLobbyServer(user, player, true));
        } else {
            return new BiHolder<>(false, plugin.getServerHandler().chooseLimboServer(user, player));
        }
    }
}
