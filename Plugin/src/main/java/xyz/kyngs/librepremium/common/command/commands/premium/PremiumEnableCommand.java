package xyz.kyngs.librepremium.common.command.commands.premium;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

import java.util.UUID;

@CommandAlias("premium|autologin")
public class PremiumEnableCommand<P> extends PremiumCommand<P> {

    public PremiumEnableCommand(AuthenticLibrePremium<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.premium}")
    @CommandCompletion("password")
    public void onPremium(Audience sender, UUID uuid, P player, User user, @Single String password) {
        checkCracked(user);

        var hashed = user.getHashedPassword();
        var crypto = getCrypto(hashed);

        if (!crypto.matches(password, hashed)) {
            throw new InvalidCommandArgument(getMessage("error-password-wrong"));
        }

        plugin.getCommandProvider().registerConfirm(uuid);

        sender.sendMessage(getMessage("prompt-confirm"));

    }

}
