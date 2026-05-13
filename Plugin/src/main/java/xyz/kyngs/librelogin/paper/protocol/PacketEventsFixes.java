/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.reflection.ReflectionObject;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramChannel;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

public final class PacketEventsFixes {

    private PacketEventsFixes() {
    }

    public static int skipUdpServerChannels() {
        var connection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();
        if (connection == null) return 0;

        var patched = 0;
        var seen = Collections.newSetFromMap(new IdentityHashMap<Channel, Boolean>());
        var wrapper = new ReflectionObject(connection);

        for (var i = 0; i < 2; i++) {
            for (var channel : readChannels(wrapper, i)) {
                if (seen.add(channel) && patchUdpChannel(channel)) {
                    patched++;
                }
            }
        }

        return patched;
    }

    private static List<Channel> readChannels(ReflectionObject wrapper, int index) {
        try {
            List<?> futures = wrapper.readList(index);
            return futures.stream()
                    .filter(future -> future instanceof ChannelFuture)
                    .map(future -> ((ChannelFuture) future).channel())
                    .toList();
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private static boolean patchUdpChannel(Channel channel) {
        if (!(channel instanceof DatagramChannel)) return false;

        var pipeline = channel.pipeline();
        var handler = pipeline.get(PacketEvents.CONNECTION_HANDLER_NAME);
        if (handler == null || handler instanceof UdpPassthroughHandler) return false;

        pipeline.replace(
                PacketEvents.CONNECTION_HANDLER_NAME,
                PacketEvents.CONNECTION_HANDLER_NAME,
                new UdpPassthroughHandler()
        );
        return true;
    }

    private static final class UdpPassthroughHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
            context.fireChannelRead(message);
        }
    }
}
