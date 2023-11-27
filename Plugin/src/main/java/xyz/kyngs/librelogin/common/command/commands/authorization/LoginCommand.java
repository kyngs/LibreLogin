/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.api.event.events.WrongPasswordEvent.AuthenticationSource;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.event.events.AuthenticWrongPasswordEvent;

import java.util.concurrent.CompletionStage;

@CommandAlias("login|l|log")
public class LoginCommand<P> extends AuthorizationCommand<P> {

    public LoginCommand(AuthenticLibreLogin<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.login}")
    @CommandCompletion("%autocomplete.login")
    public CompletionStage<Void> onLogin(Audience sender, P player, @Single String password, @Optional String code) {
        return runAsync(() -> {
            checkUnauthorized(player);
            var user = getUser(player);
            if (!user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-not-registered"));

            sender.sendMessage(getMessage("info-logging-in"));

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (crypto == null) throw new InvalidCommandArgument(getMessage("error-password-corrupted"));

            if (!crypto.matches(password, hashed)) {
                plugin.getEventProvider()
                        .unsafeFire(plugin.getEventTypes().wrongPassword,
                                new AuthenticWrongPasswordEvent<>(user, player, plugin, AuthenticationSource.LOGIN));
                if (plugin.getConfiguration().get(ConfigurationKeys.KICK_ON_WRONG_PASSWORD)) {
                    plugin.getPlatformHandle().kick(player, getMessage("kick-error-password-wrong"));
                }
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            var secret = user.getSecret();

            if (secret != null) {
                var totp = plugin.getTOTPProvider();

                if (totp != null) {
                    if (code == null) throw new InvalidCommandArgument(getMessage("totp-not-provided"));

                    int parsedCode;

                    try {
                        parsedCode = Integer.parseInt(code.trim().replace(" ", ""));
                    } catch (NumberFormatException e) {
                        throw new InvalidCommandArgument(getMessage("totp-wrong"));
                    }

                    if (!totp.verify(parsedCode, secret)) {
                        if (plugin.getConfiguration().get(ConfigurationKeys.KICK_ON_WRONG_PASSWORD)) {
                            plugin.getPlatformHandle().kick(player, getMessage("kick-error-totp-wrong"));
                        }
                        throw new InvalidCommandArgument(getMessage("totp-wrong"));
                    }
                }
            }

            sender.sendMessage(getMessage("info-logged-in"));
            getAuthorizationProvider().authorize(user, player, AuthenticatedEvent.AuthenticationReason.LOGIN);
        });
    }

}
