package xyz.kyngs.librepremium.common.command.commands.premium;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

import java.util.UUID;

@CommandAlias("premiumconfirm|confirmpremium")
public class PremiumConfirmCommand extends PremiumCommand {
    public PremiumConfirmCommand(AuthenticLibrePremium plugin) {
        super(plugin);
    }

    @Default
    public void onPremiumConfirm(Audience sender, UUID uuid, User user) {
        checkAuthorized(user);
        checkCracked(user);

        plugin.getCommandProvider().onConfirm(uuid, sender, user);
    }

}
