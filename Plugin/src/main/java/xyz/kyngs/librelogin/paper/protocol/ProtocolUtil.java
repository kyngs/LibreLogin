/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.reflection.ReflectionObject;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;

import static io.github.retrooper.packetevents.util.SpigotReflectionUtil.CHANNEL_CLASS;
import static io.github.retrooper.packetevents.util.SpigotReflectionUtil.NETWORK_MANAGER_CLASS;

public class ProtocolUtil {
    public static ServerVersion getServerVersion() {
        return PacketEvents.getAPI().getServerManager().getVersion();
    }

    public static Channel getChannel(Object networkManager) {
        ReflectionObject wrapper = new ReflectionObject(networkManager, NETWORK_MANAGER_CLASS);
        return (Channel) wrapper.readObject(0, CHANNEL_CLASS);
    }

    public static Object findNetworkManager(Object channel) {
        var managers = SpigotReflectionUtil.getNetworkManagers();
        for (Object manager : managers) {
            var managerChannel = (Channel) getChannel(manager);
            if (managerChannel.remoteAddress().equals(((Channel) channel).remoteAddress())) {
                return manager;
            }
        }
        return null;
    }
}
