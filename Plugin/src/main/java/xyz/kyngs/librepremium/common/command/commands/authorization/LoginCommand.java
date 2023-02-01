package xyz.kyngs.librepremium.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

import java.util.concurrent.CompletionStage;

@CommandAlias("login|l|log")
public class LoginCommand<P> extends AuthorizationCommand<P> {

    public LoginCommand(AuthenticLibrePremium<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.login}")
    @CommandCompletion("%autocomplete.login")
    public CompletionStage<Void> onLogin(Audience sender, P player, @Single String password, @Optional Integer code) {
        return runAsync(() -> {
            checkUnauthorized(player);
            var user = getUser(player);
            if (!user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-not-registered"));

            sender.sendMessage(getMessage("info-logging-in"));

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (crypto == null) throw new InvalidCommandArgument(getMessage("error-password-corrupted"));

            if (!crypto.matches(password, hashed)) {
                if (plugin.getConfiguration().kickOnWrongPassword()) {
                    plugin.getPlatformHandle().kick(player, getMessage("kick-error-password-wrong"));
                }
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            var secret = user.getSecret();

            if (secret != null) {
                var totp = plugin.getTOTPProvider();

                if (totp != null) {
                    if (code == null) throw new InvalidCommandArgument(getMessage("totp-not-provided"));

                    if (!totp.verify(code, secret)) {
                        if (plugin.getConfiguration().kickOnWrongPassword()) {
                            plugin.getPlatformHandle().kick(player, getMessage("kick-error-totp-wrong"));
                        }
                        throw new InvalidCommandArgument(getMessage("totp-wrong"));
                    }
                }
            }

            sender.sendMessage(getMessage("info-logged-in"));
            getAuthorizationProvider().authorize(user, player, AuthenticatedEvent.AuthenticationReason.LOGIN);
        });
    }

}
