package xyz.kyngs.librelogin.common.integration.nanolimbo;

import java.util.Collection;
import java.util.Collections;

import ua.nanit.limbo.server.Command;
import ua.nanit.limbo.server.CommandHandler;

public class DummyCommandHandler implements CommandHandler<Command> {
    @Override
    public Collection<Command> getCommands() {
        return Collections.emptyList();
    }

    @Override
    public void register(Command command) {
    }

    @Override
    public boolean executeCommand(String input) {
        return false;
    }
}
