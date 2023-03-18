/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common;

import xyz.kyngs.librelogin.api.Logger;

import java.util.function.Supplier;

public class SLF4JLogger implements Logger {

    private final org.slf4j.Logger slf4j;
    private final Supplier<Boolean> debug;

    public SLF4JLogger(org.slf4j.Logger slf4j, Supplier<Boolean> debug) {
        this.slf4j = slf4j;
        this.debug = debug;
    }

    @Override
    public void info(String message) {
        slf4j.info(message);
    }

    @Override
    public void warn(String message) {
        slf4j.warn(message);
    }

    @Override
    public void error(String message) {
        slf4j.error(message);
    }

    @Override
    public void debug(String message) {
        if (debug.get()) {
            slf4j.info("[DEBUG] " + message);
        }
    }
}
