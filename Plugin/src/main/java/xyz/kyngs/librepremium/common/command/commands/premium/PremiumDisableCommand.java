package xyz.kyngs.librepremium.common.command.commands.premium;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.event.events.AuthenticPremiumLoginSwitchEvent;

import java.util.UUID;

@CommandAlias("cracked|manuallogin")
public class PremiumDisableCommand extends PremiumCommand {

    public PremiumDisableCommand(AuthenticLibrePremium premium) {
        super(premium);
    }

    @Default
    public void onCracked(Audience sender, UUID uuid, User user) {
        checkAuthorized(user);
        checkPremium(user);

        sender.sendMessage(getMessage("info-disabling"));

        user.setPremiumUUID(null);

        plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent(user, sender));

        getDatabaseProvider().updateUser(user);

        plugin.kick(uuid, getMessage("kick-premium-info-disabled"));
    }

}
