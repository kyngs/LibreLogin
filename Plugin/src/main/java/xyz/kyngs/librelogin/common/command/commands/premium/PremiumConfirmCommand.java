package xyz.kyngs.librelogin.common.command.commands.premium;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

import java.util.concurrent.CompletionStage;

@CommandAlias("premiumconfirm|confirmpremium")
public class PremiumConfirmCommand<P> extends PremiumCommand<P> {
    public PremiumConfirmCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Default
    public CompletionStage<Void> onPremiumConfirm(Audience sender, P player) {
        return runAsync(() -> {
            var user = getUser(player);
            checkCracked(user);

            plugin.getCommandProvider().onConfirm(player, sender, user);
        });
    }

}
