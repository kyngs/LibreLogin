package xyz.kyngs.librepremium.common.listener;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.database.User;

public record PreLoginResult(PreLoginState state, @Nullable Component message, User user) {
}
