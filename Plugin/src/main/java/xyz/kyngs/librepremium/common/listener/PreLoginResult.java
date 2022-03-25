package xyz.kyngs.librepremium.common.listener;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public record PreLoginResult(PreLoginState state, @Nullable Component message) {
}
