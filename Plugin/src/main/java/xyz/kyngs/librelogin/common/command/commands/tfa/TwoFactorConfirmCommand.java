/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.tfa;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.Command;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;

import java.util.concurrent.CompletionStage;

@CommandAlias("2faconfirm")
public class TwoFactorConfirmCommand<P> extends Command<P> {
    public TwoFactorConfirmCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.2fa-confirm}")
    @CommandCompletion("%autocomplete.2fa-confirm")
    public CompletionStage<Void> onTwoFactorConfirm(Audience sender, P player, String code) {
        return runAsync(() -> {
            checkAuthorized(player);
            var user = getUser(player);
            var auth = plugin.getAuthorizationProvider();

            if (!auth.isAwaiting2FA(player)) {
                throw new InvalidCommandArgument(getMessage("totp-not-awaiting"));
            }

            int parsedCode;

            try {
                parsedCode = Integer.parseInt(code.trim().replace(" ", ""));
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(getMessage("totp-wrong"));
            }

            if (!auth.confirmTwoFactorAuth(player, parsedCode, user)) {
                throw new InvalidCommandArgument(getMessage("totp-wrong"));
            }

            plugin.getPlatformHandle().kick(player, getMessage("kick-2fa-enabled"));
        });
    }
}
