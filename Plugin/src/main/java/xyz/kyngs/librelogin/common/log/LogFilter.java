/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.log;

import java.util.Set;

/**
 * A filter that prevents sensitive authentication commands from being logged.
 * <p>
 * This abstract class provides functionality to filter out commands that might contain sensitive information such as passwords or authentication tokens.
 */
public abstract class LogFilter {

    /**
     * A set of command prefixes that should be protected from logging.
     * <p>
     * These commands typically contain sensitive information like passwords.
     */
    private static final Set<String> PROTECTED_COMMANDS = Set.of(
        "/login ",
        "/l ",
        "/log ",
        "/register ",
        "/reg ",
        "/premium ",
        "/autologin ",
        "/2faconfirm ",
        "/changepassword ",
        "/changepass ",
        "/passch ",
        "/passwd ",
        "/confirmpasswordreset ",
        "/setemail ",
        "/librelogin user register ",
        "/librelogin user pass-change "
    );

    /**
     * Checks if a log message containing a command should be filtered out.
     *
     * @param message The message pattern being logged
     * @param parameters The parameters being used in the message.
     * @return {@code true} if the message should be logged, {@code false} if it should be filtered out
     */
    protected boolean checkMessage(String message, Object[] parameters) {
        if (parameters == null || parameters.length <= 1 || !(parameters[1] instanceof String parameter)) return true;

        parameter = switch (message) {
            case "{} issued server command: {}" -> parameter;
            case "{0} executed command: /{1}", "{} -> executed command /{}" -> '/' + parameter;
            default -> "";
        };

        if (parameter.isEmpty()) return true;

        for (String command : PROTECTED_COMMANDS) {
            if (parameter.startsWith(command)) return false;
        }

        return true;
    }

    /**
     * Injects this filter into the logging system.
     */
    public abstract void inject();

}
