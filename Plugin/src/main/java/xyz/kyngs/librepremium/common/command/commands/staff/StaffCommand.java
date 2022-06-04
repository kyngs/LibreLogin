package xyz.kyngs.librepremium.common.command.commands.staff;

import org.jetbrains.annotations.NotNull;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.Command;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;

public class StaffCommand<P> extends Command<P> {
    public StaffCommand(AuthenticLibrePremium<P, ?> plugin) {
        super(plugin);
    }

    @NotNull
    protected User getUserOtherWiseInform(String name) {
        var user = plugin.getDatabaseProvider().getByName(name);

        if (user == null) throw new InvalidCommandArgument(getMessage("error-unknown-user"));

        return user;
    }

    protected void requireOffline(User user) {
        if (plugin.isPresent(user.getUuid()))
            throw new InvalidCommandArgument(getMessage("error-player-online"));
    }

    protected P requireOnline(User user) {
        if (plugin.multiProxyEnabled())
            throw new InvalidCommandArgument(getMessage("error-not-available-on-multi-proxy"));
        var player = plugin.getPlayerForUUID(user.getUuid());
        if (player == null)
            throw new InvalidCommandArgument(getMessage("error-player-offline"));
        return player;
    }

    protected void requireUnAuthorized(P player) {
        if (plugin.getAuthorizationProvider().isAuthorized(player))
            throw new InvalidCommandArgument(getMessage("error-player-authorized"));
    }

    protected void requireRegistered(User user) {
        if (!user.isRegistered())
            throw new InvalidCommandArgument(getMessage("error-player-not-registered"));
    }

}
