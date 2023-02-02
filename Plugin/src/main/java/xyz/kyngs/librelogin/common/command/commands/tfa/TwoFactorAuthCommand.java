package xyz.kyngs.librelogin.common.command.commands.tfa;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.common.AuthenticLibrePremium;
import xyz.kyngs.librelogin.common.command.Command;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;

import java.util.concurrent.CompletionStage;

@CommandAlias("2fa|2fauth|2fauthcode")
public class TwoFactorAuthCommand<P> extends Command<P> {
    public TwoFactorAuthCommand(AuthenticLibrePremium<P, ?> plugin) {
        super(plugin);
    }

    @Default
    public CompletionStage<Void> onTwoFactorAuth(Audience sender, P player) {
        return runAsync(() -> {
            checkAuthorized(player);
            var user = getUser(player);
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

            sender.sendMessage(getMessage("totp-generating"));

            var data = plugin.getTOTPProvider().generate(user);

            auth.beginTwoFactorAuth(user, player, data);

            plugin.cancelOnExit(plugin.delay(() -> {
                plugin.getImageProjector().project(data.qr(), player);

                sender.sendMessage(getMessage("totp-show-info"));
            }, 250), player);
        });
    }
}
