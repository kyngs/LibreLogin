/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.log;

import java.util.HashSet;
import java.util.Set;

public abstract class LogFilter {

    private static final Set<String> PROTECTED_COMMANDS;

    static {
        PROTECTED_COMMANDS = new HashSet<>();

        PROTECTED_COMMANDS.add("login");
        PROTECTED_COMMANDS.add("l");
        PROTECTED_COMMANDS.add("log");
        PROTECTED_COMMANDS.add("register");
        PROTECTED_COMMANDS.add("reg");
        PROTECTED_COMMANDS.add("premium");
        PROTECTED_COMMANDS.add("autologin");
        PROTECTED_COMMANDS.add("2faconfirm");
        PROTECTED_COMMANDS.add("changepassword");
        PROTECTED_COMMANDS.add("changepass");
        PROTECTED_COMMANDS.add("passch");
        PROTECTED_COMMANDS.add("passwd");
        PROTECTED_COMMANDS.add("confirmpasswordreset");
        PROTECTED_COMMANDS.add("setemail");
        PROTECTED_COMMANDS.add("librelogin user register");
        PROTECTED_COMMANDS.add("librelogin user pass-change");
    }

    protected boolean checkMessage(String message) {
        // This sucks, but it's the only way to filter out the spam from the plugin
        if (message.contains("Plugin listener xyz.kyngs.librelogin.bungeecord.BungeeCordListener took")) return false;
        if (!message.contains("issued server command: /") && !message.contains("executed command /") && !message.contains("executed command: /") && !message.contains("Duplicate key name"))
            return true;

        for (String command : PROTECTED_COMMANDS) {
            if (message.contains(command)) return false;
        }

        return true;
    }

    public abstract void inject();

}
