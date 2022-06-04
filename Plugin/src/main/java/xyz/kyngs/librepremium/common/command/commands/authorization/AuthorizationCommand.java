package xyz.kyngs.librepremium.common.command.commands.authorization;

import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

public class AuthorizationCommand<P> extends Command<P> {

    public AuthorizationCommand(AuthenticLibrePremium<P, ?> premium) {
        super(premium);
    }

    protected void checkUnauthorized(P player) {
        if (getAuthorizationProvider().isAuthorized(player)) {
            throw new InvalidCommandArgument(getMessage("error-already-authorized"));
        }
    }

}
