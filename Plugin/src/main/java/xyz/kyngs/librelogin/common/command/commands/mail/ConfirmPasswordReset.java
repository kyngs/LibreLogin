/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.mail;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.event.events.AuthenticPasswordChangeEvent;

import java.util.concurrent.CompletionStage;

@CommandAlias("confirmpasswordreset")
public class ConfirmPasswordReset<P> extends EMailCommand<P> {
    public ConfirmPasswordReset(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.confirm-password-reset}")
    @CommandCompletion("%autocomplete.confirm-password-reset")
    public CompletionStage<Void> onConfirmPassReset(Audience audience, P player, String token, String password, @Single String passwordRepeat) {
        return runAsync(() -> {
            var user = getUser(player);

            var cached = plugin.getAuthorizationProvider().getPasswordResetCache().getIfPresent(user.getUuid());
            if (cached == null) {
                throw new InvalidCommandArgument(getMessage("error-no-password-reset"));
            }
            if (!cached.equals(token)) {
                throw new InvalidCommandArgument(getMessage("error-wrong-password-reset"));
            }
            if (!password.equals(passwordRepeat)) {
                throw new InvalidCommandArgument(getMessage("error-password-not-match"));
            }

            var old = user.getHashedPassword();
            setPassword(audience, user, password, "info-resetting-password");

            plugin.getAuthorizationProvider().getPasswordResetCache().invalidate(user.getUuid());
            getDatabaseProvider().updateUser(user);
            audience.sendMessage(getMessage("info-password-reset"));

            plugin.getEventProvider().unsafeFire(plugin.getEventTypes().passwordChange, new AuthenticPasswordChangeEvent<>(user, player, plugin, old));
        });
    }

}
