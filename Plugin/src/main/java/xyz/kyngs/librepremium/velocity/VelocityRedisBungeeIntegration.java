package xyz.kyngs.librepremium.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class VelocityRedisBungeeIntegration {

    private final RedisBungeeAPI redisBungeeAPI;

    public VelocityRedisBungeeIntegration() {
        redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
    }

    public boolean isPlayerOnline(@NonNull UUID player) {
        return redisBungeeAPI.isPlayerOnline(player);
    }
}
