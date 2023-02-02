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
