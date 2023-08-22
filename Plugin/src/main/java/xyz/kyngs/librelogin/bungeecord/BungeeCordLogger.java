/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord;

import xyz.kyngs.librelogin.api.Logger;

import java.util.function.Supplier;
import java.util.logging.Level;

public class BungeeCordLogger implements Logger {

    private final BungeeCordBootstrap bootstrap;
    private final Supplier<Boolean> debug;

    public BungeeCordLogger(BungeeCordBootstrap bootstrap, Supplier<Boolean> debug) {
        this.bootstrap = bootstrap;
        this.debug = debug;
    }

    @Override
    public void info(String message) {
        bootstrap.getLogger().info(message);
    }

    @Override
    public void warn(String message) {
        bootstrap.getLogger().warning(message);
    }

    @Override
    public void error(String message) {
        bootstrap.getLogger().severe(message);
    }

    @Override
    public void debug(String message) {
        if (debug.get()) {
            bootstrap.getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }

}
