/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
