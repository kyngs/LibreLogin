package xyz.kyngs.librepremium.common.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

import java.util.UUID;

@CommandAlias("2fa|2fauth|2fauthcode")
public class TwoFactorAuthCommand extends Command {
    public TwoFactorAuthCommand(AuthenticLibrePremium plugin) {
        super(plugin);
    }

    @Default
    public void onTwoFactorAuth(Audience sender, UUID id, User user) {
        checkAuthorized(user);
        var auth = plugin.getAuthorizationProvider();

        if (auth.isAwaiting2FA(id)) {
            throw new InvalidCommandArgument(getMessage("totp-show-info"));
        }

        auth.beginTwoFactorAuth(id, sender, user, plugin.getPlayerForUUID(id));

        sender.sendMessage(getMessage("totp-generating"));

        plugin.cancelOnExit(plugin.delay(() -> {
            var data = plugin.getTOTPProvider().generate(user);

            user.setSecret(data.secret());

            plugin.getDatabaseProvider().updateUser(user);

            plugin.getImageProjector().project(data.qr(), plugin.getPlayerForUUID(id));

            sender.sendMessage(getMessage("totp-show-info"));
        }, 250), id);
    }
}
