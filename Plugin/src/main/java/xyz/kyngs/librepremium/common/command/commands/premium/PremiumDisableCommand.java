package xyz.kyngs.librepremium.common.command.commands.premium;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.event.events.AuthenticPremiumLoginSwitchEvent;

@CommandAlias("cracked|manuallogin")
public class PremiumDisableCommand<P> extends PremiumCommand<P> {

    public PremiumDisableCommand(AuthenticLibrePremium<P, ?> premium) {
        super(premium);
    }

    @Default
    public void onCracked(Audience sender, P player, User user) {
        checkAuthorized(player);
        checkPremium(user);

        sender.sendMessage(getMessage("info-disabling"));

        user.setPremiumUUID(null);

        plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent<>(user, player, plugin));

        getDatabaseProvider().updateUser(user);

        plugin.getPlatformHandle().kick(player, getMessage("kick-premium-info-disabled"));
    }

}
