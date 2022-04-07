package xyz.kyngs.librepremium.common.command.commands.staff;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

public class StaffCommand extends Command {
    public StaffCommand(AuthenticLibrePremium plugin) {
        super(plugin);
    }

    @NotNull
    protected User getUserOtherWiseInform(String name) {
        var user = plugin.getDatabaseProvider().getByName(name);

        if (user == null) throw new InvalidCommandArgument(getMessage("error-unknown-user"));

        return user;
    }

    protected void requireOffline(User user) {
        if (plugin.getAudienceForID(user.getUuid()) != null)
            throw new InvalidCommandArgument(getMessage("error-player-online"));
    }

    protected Audience requireOnline(User user) {
        var audience = plugin.getAudienceForID(user.getUuid());
        if (audience == null)
            throw new InvalidCommandArgument(getMessage("error-player-offline"));
        return audience;
    }

    protected void requireUnAuthorized(User user) {
        if (plugin.getAuthorizationProvider().isAuthorized(user.getUuid()))
            throw new InvalidCommandArgument(getMessage("error-player-authorized"));
    }

    protected void requireRegistered(User user) {
        if (!user.isRegistered())
            throw new InvalidCommandArgument(getMessage("error-player-not-registered"));
    }

}
