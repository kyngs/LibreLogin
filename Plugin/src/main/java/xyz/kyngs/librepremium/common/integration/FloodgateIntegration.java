package xyz.kyngs.librepremium.common.integration;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class FloodgateIntegration {

    private final FloodgateApi api;

    public FloodgateIntegration() {
        api = FloodgateApi.getInstance();
    }

    public boolean isFloodgateId(UUID uuid) {
        return api.isFloodgateId(uuid);
    }


}
