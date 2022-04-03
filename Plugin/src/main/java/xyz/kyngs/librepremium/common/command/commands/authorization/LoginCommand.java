package xyz.kyngs.librepremium.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

import java.util.UUID;

@CommandAlias("login|l")
public class LoginCommand extends AuthorizationCommand {

    public LoginCommand(AuthenticLibrePremium premium) {
        super(premium);
    }

    @Default
    @Syntax("<password>")
    @CommandCompletion("password")
    public void onLogin(Audience sender, UUID uuid, User user, @Single String password) {
        checkUnauthorized(user);
        if (!user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-not-registered"));

        sender.sendMessage(getMessage("info-logging-in"));

        var hashed = user.getHashedPassword();
        var crypto = getCrypto(hashed);

        if (crypto == null) throw new InvalidCommandArgument(getMessage("error-password-corrupted"));

        if (!crypto.matches(password, hashed)) {
            if (plugin.getConfiguration().kickOnWrongPassword()) {
                plugin.kick(uuid, getMessage("error-password-wrong"));
            }
            throw new InvalidCommandArgument(getMessage("error-password-wrong"));
        }

        sender.sendMessage(getMessage("info-logged-in"));
        getAuthorizationProvider().authorize(uuid, user, sender);
    }

}
