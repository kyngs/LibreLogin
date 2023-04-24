/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.api.event.events.PasswordChangeEvent;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.Command;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.event.events.AuthenticPasswordChangeEvent;

import java.util.concurrent.CompletionStage;

@CommandAlias("changepassword|changepass|passwd|passch")
public class ChangePasswordCommand<P> extends Command<P> {
    public ChangePasswordCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.change-password}")
    @CommandCompletion("%autocomplete.change-password")
    public CompletionStage<Void> onPasswordChange(Audience sender, P player, String oldPass, @Single String newPass) {
        return runAsync(() -> {
            sender.sendMessage(getMessage("info-editing"));
            var user = getUser(player);

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (!crypto.matches(oldPass, hashed)) {
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            var defaultProvider = plugin.getDefaultCryptoProvider();

            var hash = defaultProvider.createHash(newPass);

            if (hash == null) {
                throw new InvalidCommandArgument(getMessage("error-password-too-long"));
            }

            user.setHashedPassword(hash);

            getDatabaseProvider().updateUser(user);

            sender.sendMessage(getMessage("info-edited"));

            plugin.getEventProvider().fire(PasswordChangeEvent.class, new AuthenticPasswordChangeEvent<>(user, player, plugin, hashed));
        });
    }

}
