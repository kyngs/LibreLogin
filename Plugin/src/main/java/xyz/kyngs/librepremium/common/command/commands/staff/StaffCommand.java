package xyz.kyngs.librepremium.common.command.commands.staff;

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

}
