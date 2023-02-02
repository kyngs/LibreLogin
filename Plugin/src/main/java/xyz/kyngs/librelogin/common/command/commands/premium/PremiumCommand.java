package xyz.kyngs.librelogin.common.command.commands.premium;

import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.common.AuthenticLibrePremium;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.command.commands.authorization.AuthorizationCommand;

public class PremiumCommand<P> extends AuthorizationCommand<P> {
    public PremiumCommand(AuthenticLibrePremium<P, ?> premium) {
        super(premium);
    }

    protected void checkCracked(User user) {
        if (user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-cracked"));
    }

    protected void checkPremium(User user) {
        if (!user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-premium"));
    }

}
