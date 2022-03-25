package xyz.kyngs.librepremium.common.command.commands.premium;

import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;
import xyz.kyngs.librepremium.common.command.commands.authorization.AuthorizationCommand;

public class PremiumCommand extends AuthorizationCommand {
    public PremiumCommand(AuthenticLibrePremium premium) {
        super(premium);
    }

    protected void checkCracked(User user) {
        if (user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-cracked"));
    }

    protected void checkPremium(User user) {
        if (!user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-premium"));
    }

}
