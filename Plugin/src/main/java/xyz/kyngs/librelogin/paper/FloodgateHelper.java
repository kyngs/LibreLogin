/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

public class FloodgateHelper {

    private final PlayerInjectionHandler handler;

    public FloodgateHelper() {
        PacketFilterManager manager = (PacketFilterManager) ProtocolLibrary.getProtocolManager();
        FieldAccessor accessor = Accessors.getFieldAccessor(manager.getClass(), PlayerInjectionHandler.class, true);
        handler = (PlayerInjectionHandler) accessor.get(manager);
    }

    /**
     * Reimplementation of the tasks injected Floodgate in ProtocolLib that are not run due to a bug
     *
     * @param packetEvent the PacketEvent that won't be processed by Floodgate
     * @return false if the player was kicked
     * @author games647 and FastLogin contributors
     * @see <a href="https://github.com/GeyserMC/Floodgate/issues/143">Issue Floodgate#143</a>
     * @see <a href="https://github.com/GeyserMC/Floodgate/blob/5d5713ed9e9eeab0f4abdaa9cf5cd8619dc1909b/spigot/src/main/java/org/geysermc/floodgate/addon/data/SpigotDataHandler.java#L121-L175">Floodgate/SpigotDataHandler</a>
     */
    protected boolean processFloodgateTasks(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Player player = packetEvent.getPlayer();
        FloodgatePlayer floodgatePlayer = getFloodgatePlayer(player);
        if (floodgatePlayer == null) {
            return true;
        }

        // kick the player, if necessary
        Channel channel = handler.getChannel(packetEvent.getPlayer());
        AttributeKey<String> kickMessageAttribute = AttributeKey.valueOf("floodgate-kick-message");
        String kickMessage = channel.attr(kickMessageAttribute).get();
        if (kickMessage != null) {
            player.kickPlayer(kickMessage);
            return false;
        }

        // add prefix
        String username = floodgatePlayer.getCorrectUsername();
        if (packet.getGameProfiles().size() > 0) {
            packet.getGameProfiles().write(0,
                    new WrappedGameProfile(floodgatePlayer.getCorrectUniqueId(), username));
        } else {
            packet.getStrings().write(0, username);
        }

        // remove real Floodgate data handler
        ChannelHandler floodgateHandler = channel.pipeline().get("floodgate_data_handler");
        channel.pipeline().remove(floodgateHandler);

        return true;
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private FloodgatePlayer getFloodgatePlayer(Player player) {
        Channel channel = handler.getChannel(player);
        AttributeKey<FloodgatePlayer> floodgateAttribute = AttributeKey.valueOf("floodgate-player");
        return channel.attr(floodgateAttribute).get();
    }
}
