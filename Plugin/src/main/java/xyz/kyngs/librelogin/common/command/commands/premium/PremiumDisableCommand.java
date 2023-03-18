/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.premium;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.event.events.AuthenticPremiumLoginSwitchEvent;

import java.util.concurrent.CompletionStage;

@CommandAlias("cracked|manuallogin")
public class PremiumDisableCommand<P> extends PremiumCommand<P> {

    public PremiumDisableCommand(AuthenticLibreLogin<P, ?> premium) {
        super(premium);
    }

    @Default
    public CompletionStage<Void> onCracked(Audience sender, P player) {
        return runAsync(() -> {
            var user = getUser(player);
            checkPremium(user);

            sender.sendMessage(getMessage("info-disabling"));

            user.setPremiumUUID(null);

            plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent<>(user, player, plugin));

            getDatabaseProvider().updateUser(user);

            plugin.getPlatformHandle().kick(player, getMessage("kick-premium-info-disabled"));
        });
    }

}
