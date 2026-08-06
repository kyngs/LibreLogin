/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

public class FloodgateHelper {

    /**
     * Reimplementation of the tasks injected Floodgate in ProtocolLib that are not run due to a bug
     *
     * @param event the PacketEvent that won't be processed by Floodgate
     * @return false if the player was kicked
     * @author games647 and FastLogin contributors
     * @see <a href="https://github.com/GeyserMC/Floodgate/issues/143">Issue Floodgate#143</a>
     * @see <a href="https://github.com/GeyserMC/Floodgate/blob/5d5713ed9e9eeab0f4abdaa9cf5cd8619dc1909b/spigot/src/main/java/org/geysermc/floodgate/addon/data/SpigotDataHandler.java#L121-L175">Floodgate/SpigotDataHandler</a>
     */
    protected boolean processFloodgateTasks(PacketReceiveEvent event, WrapperLoginClientLoginStart packet) {
        FloodgatePlayer floodgatePlayer = getFloodgatePlayer(event.getChannel());
        if (floodgatePlayer == null) {
            return true;
        }

        // kick the player, if necessary
        Channel channel = (Channel) event.getChannel();
        AttributeKey<String> kickMessageAttribute = AttributeKey.valueOf("floodgate-kick-message");
        String kickMessage = channel.attr(kickMessageAttribute).get();
        if (kickMessage != null) {
            event.getUser().closeConnection();
            return false;
        }

        // add prefix
        String username = floodgatePlayer.getCorrectUsername();
        packet.setUsername(username);

        // remove real Floodgate data handler
        ChannelHandler floodgateHandler = channel.pipeline().get("floodgate_data_handler");
        channel.pipeline().remove(floodgateHandler);

        return true;
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private FloodgatePlayer getFloodgatePlayer(Object channel) {
        AttributeKey<FloodgatePlayer> floodgateAttribute = AttributeKey.valueOf("floodgate-player");
        return ((Channel) channel).attr(floodgateAttribute).get();
    }
}
