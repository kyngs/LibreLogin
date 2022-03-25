package xyz.kyngs.librepremium.common.command.commands.authorization;

import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

public class AuthorizationCommand extends Command {

    public AuthorizationCommand(AuthenticLibrePremium premium) {
        super(premium);
    }

    protected void checkUnauthorized(User user) {
        if (getAuthorizationProvider().isAuthorized(user.getUuid())) {
            throw new InvalidCommandArgument(getMessage("error-already-authorized"));
        }
    }

}
