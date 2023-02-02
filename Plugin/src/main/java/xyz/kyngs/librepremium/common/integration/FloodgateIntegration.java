package xyz.kyngs.librepremium.common.integration;

import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.UUID;

public class FloodgateIntegration {

    private final FloodgateApi api;

    public FloodgateIntegration() {
        api = FloodgateApi.getInstance();
    }

    public boolean isFloodgateId(UUID uuid) {
        return api.isFloodgatePlayer(uuid);
    }

    public FloodgatePlayer getPlayer(String username) {
        for (FloodgatePlayer floodgatePlayer : api.getPlayers()) {
            if (floodgatePlayer.getCorrectUsername().equals(username)) {
                return floodgatePlayer;
            }
        }

        return null;
    }


}
