/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command;

import net.kyori.adventure.text.TextComponent;

public class InvalidCommandArgument extends RuntimeException {

    private final TextComponent userFuckUp;

    public InvalidCommandArgument(TextComponent userFuckUp) {
        this.userFuckUp = userFuckUp;
    }

    public TextComponent getUserFuckUp() {
        return userFuckUp;
    }
}
