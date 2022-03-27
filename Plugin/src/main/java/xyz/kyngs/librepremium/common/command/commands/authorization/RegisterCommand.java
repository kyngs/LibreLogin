package xyz.kyngs.librepremium.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

import java.util.UUID;

@CommandAlias("register|r")
public class RegisterCommand extends AuthorizationCommand {
    public RegisterCommand(AuthenticLibrePremium premium) {
        super(premium);
    }

    @Default
    @Syntax("<password> <passwordRepeat>")
    @CommandCompletion("password password")
    public void onRegister(Audience sender, UUID uuid, User user, @Single String password, String passwordRepeat) {
        checkUnauthorized(user);

        if (!password.contentEquals(passwordRepeat))
            throw new InvalidCommandArgument(getMessage("error-password-not-match"));
        if (user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-already-registered"));

        sender.sendMessage(getMessage("info-registering"));

        var provider = plugin.getDefaultCryptoProvider();

        user.setHashedPassword(provider.createHash(password));

        getDatabaseProvider().saveUser(user);

        sender.sendMessage(getMessage("info-registered"));

        getAuthorizationProvider().authorize(uuid, user, sender);


    }

}
