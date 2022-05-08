package xyz.kyngs.librepremium.common.image.packet;

import dev.simplix.protocolize.api.PacketDirection;
import dev.simplix.protocolize.api.mapping.ProtocolIdMapping;
import dev.simplix.protocolize.api.packet.AbstractPacket;
import dev.simplix.protocolize.api.util.ProtocolUtil;
import io.netty.buffer.ByteBuf;
import xyz.kyngs.librepremium.common.image.MapData;

import java.util.Arrays;
import java.util.List;

import static dev.simplix.protocolize.api.mapping.AbstractProtocolMapping.rangedIdMapping;
import static dev.simplix.protocolize.api.util.ProtocolVersions.*;

/**
 * Used to send a map data packet to the client.
 *
 * @author kyngs
 */
public class MapDataPacket extends AbstractPacket {

    public static final List<ProtocolIdMapping> MAPPINGS = Arrays.asList(
            rangedIdMapping(MINECRAFT_1_13, MINECRAFT_1_14_4, 0x26),
            rangedIdMapping(MINECRAFT_1_15, MINECRAFT_1_15_2, 0x27),
            rangedIdMapping(MINECRAFT_1_16, MINECRAFT_1_16_1, 0x26),
            rangedIdMapping(MINECRAFT_1_16_2, MINECRAFT_1_16_5, 0x25),
            rangedIdMapping(MINECRAFT_1_17, MINECRAFT_1_18_2, 0x27)
    );

    private int mapID;
    private byte scale;
    private MapData mapData;

    public MapDataPacket(int mapID, byte scale, MapData mapData) {
        this.mapID = mapID;
        this.scale = scale;
        this.mapData = mapData;
    }

    public MapDataPacket() {
    }

    @Override
    public void read(ByteBuf byteBuf, PacketDirection packetDirection, int protocol) {
        // NO-OP
    }

    @Override
    public void write(ByteBuf byteBuf, PacketDirection packetDirection, int protocol) {
        ProtocolUtil.writeVarInt(byteBuf, mapID);
        if (protocol >= MINECRAFT_1_8) {
            byteBuf.writeByte(scale);

            if (protocol >= MINECRAFT_1_9 && protocol < MINECRAFT_1_17) {
                byteBuf.writeBoolean(false);
            }

            if (protocol >= MINECRAFT_1_14) {
                byteBuf.writeBoolean(false);
            }

            if (protocol >= MINECRAFT_1_17) {
                byteBuf.writeBoolean(false);
            } else {
                ProtocolUtil.writeVarInt(byteBuf, 0);
            }

            byteBuf.writeByte(mapData.columns());
            byteBuf.writeByte(mapData.rows());
            byteBuf.writeByte(mapData.posX());
            byteBuf.writeByte(mapData.posY());
            ProtocolUtil.writeByteArray(byteBuf, mapData.data());
        } else {
            byteBuf.writeShort(mapData.data().length + 3);
            byteBuf.writeByte(0);
            byteBuf.writeByte(mapData.posX());
            byteBuf.writeByte(mapData.posY());
            byteBuf.writeBytes(mapData.data());
        }
    }
}
