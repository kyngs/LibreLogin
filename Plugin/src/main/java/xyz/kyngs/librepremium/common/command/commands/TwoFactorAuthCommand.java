package xyz.kyngs.librepremium.common.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

@CommandAlias("2fa|2fauth|2fauthcode")
public class TwoFactorAuthCommand<P> extends Command<P> {
    public TwoFactorAuthCommand(AuthenticLibrePremium<P, ?> plugin) {
        super(plugin);
    }

    @Default
    public void onTwoFactorAuth(Audience sender, P player, User user) {
        checkAuthorized(player);
        var auth = plugin.getAuthorizationProvider();

        if (auth.isAwaiting2FA(player)) {
            throw new InvalidCommandArgument(getMessage("totp-show-info"));
        }

        if (!plugin.getImageProjector().canProject(player)) {
            throw new InvalidCommandArgument(getMessage("totp-wrong-version",
                    "%low%", "1.13",
                    "%high%", "1.19"
            ));
        }

        auth.beginTwoFactorAuth(user, player);

        sender.sendMessage(getMessage("totp-generating"));

        plugin.cancelOnExit(plugin.delay(() -> {
            var data = plugin.getTOTPProvider().generate(user);

            user.setSecret(data.secret());

            plugin.getDatabaseProvider().updateUser(user);

            plugin.getImageProjector().project(data.qr(), player);

            sender.sendMessage(getMessage("totp-show-info"));
        }, 250), player);
    }
}
