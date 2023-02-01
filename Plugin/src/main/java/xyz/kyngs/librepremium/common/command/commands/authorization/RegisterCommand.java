package xyz.kyngs.librepremium.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

import java.util.concurrent.CompletionStage;

@CommandAlias("register|reg")
public class RegisterCommand<P> extends AuthorizationCommand<P> {
    public RegisterCommand(AuthenticLibrePremium<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.register}")
    @CommandCompletion("%autocomplete.register")
    public CompletionStage<Void> onRegister(Audience sender, P player, @Single String password, String passwordRepeat) {
        return runAsync(() -> {
            checkUnauthorized(player);
            var user = getUser(player);

            if (user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-already-registered"));
            if (!password.contentEquals(passwordRepeat))
                throw new InvalidCommandArgument(getMessage("error-password-not-match"));
            if (!plugin.validPassword(password))
                throw new InvalidCommandArgument(getMessage("error-forbidden-password"));

            sender.sendMessage(getMessage("info-registering"));

            var provider = plugin.getDefaultCryptoProvider();

            user.setHashedPassword(provider.createHash(password));

            sender.sendMessage(getMessage("info-registered"));

            getAuthorizationProvider().authorize(user, player, AuthenticatedEvent.AuthenticationReason.REGISTER);
        });
    }

}
