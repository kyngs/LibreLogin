package xyz.kyngs.librepremium.common.command.commands.tfa;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

@CommandAlias("2faconfirm")
public class TwoFactorConfirmCommand<P> extends Command<P> {
    public TwoFactorConfirmCommand(AuthenticLibrePremium<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("<code>")
    public void onTwoFactorConfirm(Audience sender, P player, User user, Integer code) {
        checkAuthorized(player);
        var auth = plugin.getAuthorizationProvider();

        if (!auth.isAwaiting2FA(player)) {
            throw new InvalidCommandArgument(getMessage("totp-not-awaiting"));
        }

        if (!auth.confirmTwoFactorAuth(player, code, user)) {
            throw new InvalidCommandArgument(getMessage("totp-wrong"));
        }

        plugin.getPlatformHandle().kick(player, getMessage("kick-2fa-enabled"));
    }
}
