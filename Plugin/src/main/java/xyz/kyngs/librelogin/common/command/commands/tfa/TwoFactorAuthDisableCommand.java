/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.tfa;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.Command;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;

import java.util.concurrent.CompletionStage;

@CommandAlias("2fadisable")
public class TwoFactorAuthDisableCommand<P> extends Command<P> {
    public TwoFactorAuthDisableCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.2fa-disable}")
    @CommandCompletion("%autocomplete.2fa-disable")
    public CompletionStage<Void> onTwoFactorAuthDisable(Audience sender, P player, @Optional String code) {
        return runAsync(() -> {
            checkAuthorized(player);
            var user = getUser(player);
            var auth = plugin.getAuthorizationProvider();
            int parsedCode;

            if (auth.isAwaiting2FADisable(player) && code != null) {
                try {
                    parsedCode = Integer.parseInt(code.trim().replace(" ", ""));
                } catch (NumberFormatException e) {
                    throw new InvalidCommandArgument(getMessage("totp-wrong"));
                }

                if (!auth.confirmTwoFactorAuthDisable(player, parsedCode, user)) {
                    throw new InvalidCommandArgument(getMessage("totp-wrong"));
                }

                sender.sendMessage(getMessage("totp-disable-confirm"));
            }else{
                auth.beginTwoFactorAuthDisable(user, player);

                sender.sendMessage(getMessage("totp-disable"));
            }
        });
    }
}
