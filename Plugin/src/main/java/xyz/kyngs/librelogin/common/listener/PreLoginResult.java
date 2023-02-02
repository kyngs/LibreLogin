package xyz.kyngs.librelogin.common.listener;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.database.User;

public record PreLoginResult(PreLoginState state, @Nullable Component message, User user) {
}
