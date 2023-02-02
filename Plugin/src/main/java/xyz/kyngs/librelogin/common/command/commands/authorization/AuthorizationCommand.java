package xyz.kyngs.librelogin.common.command.commands.authorization;

import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.Command;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;

public class AuthorizationCommand<P> extends Command<P> {

    public AuthorizationCommand(AuthenticLibreLogin<P, ?> premium) {
        super(premium);
    }

    protected void checkUnauthorized(P player) {
        if (getAuthorizationProvider().isAuthorized(player)) {
            throw new InvalidCommandArgument(getMessage("error-already-authorized"));
        }
    }

}
