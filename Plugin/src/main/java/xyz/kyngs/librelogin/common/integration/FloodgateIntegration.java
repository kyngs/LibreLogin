/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.integration;

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
