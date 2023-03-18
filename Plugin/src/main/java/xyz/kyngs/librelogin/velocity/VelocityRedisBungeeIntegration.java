/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.velocity;

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
