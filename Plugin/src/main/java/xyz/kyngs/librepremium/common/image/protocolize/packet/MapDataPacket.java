package xyz.kyngs.librepremium.common.image.protocolize.packet;

import dev.simplix.protocolize.api.PacketDirection;
import dev.simplix.protocolize.api.mapping.ProtocolIdMapping;
import dev.simplix.protocolize.api.packet.AbstractPacket;
import dev.simplix.protocolize.api.util.ProtocolUtil;
import io.netty.buffer.ByteBuf;
import xyz.kyngs.librepremium.common.image.protocolize.MapData;

import java.util.Arrays;
import java.util.List;

import static dev.simplix.protocolize.api.mapping.AbstractProtocolMapping.rangedIdMapping;
import static dev.simplix.protocolize.api.util.ProtocolUtil.readVarInt;
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
            rangedIdMapping(MINECRAFT_1_17, MINECRAFT_1_18_2, 0x27),
            rangedIdMapping(MINECRAFT_1_19, MINECRAFT_1_19, 0x24)
    );

    private int mapID;
    private byte scale;
    private MapData mapData;
    private boolean trackingPosition;
    private boolean locked;
    private IconData[] iconData;

    public MapDataPacket(int mapID, byte scale, MapData mapData) {
        this.mapID = mapID;
        this.scale = scale;
        this.mapData = mapData;

        trackingPosition = false;
        iconData = new IconData[0];
        locked = false;
    }

    public MapDataPacket() {
    }

    @Override
    public void read(ByteBuf byteBuf, PacketDirection packetDirection, int protocol) {
        mapID = readVarInt(byteBuf);

        scale = byteBuf.readByte();

        if (protocol < MINECRAFT_1_17) {
            trackingPosition = byteBuf.readBoolean();

            if (protocol >= MINECRAFT_1_14) {
                locked = byteBuf.readBoolean();
            }
        } else {
            locked = byteBuf.readBoolean();

            trackingPosition = byteBuf.readBoolean();
        }

        iconData = new IconData[(protocol < MINECRAFT_1_17 || trackingPosition) ? readVarInt(byteBuf) : 0];

        for (int i = 0; i < iconData.length; i++) {
            iconData[i] = new IconData(
                    readVarInt(byteBuf),
                    byteBuf.readByte(),
                    byteBuf.readByte(),
                    byteBuf.readByte(),
                    byteBuf.readBoolean() ? ProtocolUtil.readString(byteBuf) : null
            );
        }

        var columns = byteBuf.readUnsignedByte();

        if (columns <= 0) {
            mapData = new MapData(0, 0, 0, 0, new byte[0]);
        } else {
            var rows = byteBuf.readByte();

            var x = byteBuf.readByte();
            var y = byteBuf.readByte();

            var data = ProtocolUtil.readByteArray(byteBuf);

            mapData = new MapData(columns, rows, x, y, data);
        }
    }

    @Override
    public void write(ByteBuf byteBuf, PacketDirection packetDirection, int protocol) {
        ProtocolUtil.writeVarInt(byteBuf, mapID);

        byteBuf.writeByte(scale);

        if (protocol < MINECRAFT_1_17) {
            byteBuf.writeBoolean(trackingPosition);

            if (protocol >= MINECRAFT_1_14) {
                byteBuf.writeBoolean(locked);
            }
        } else {
            byteBuf.writeBoolean(locked);
            byteBuf.writeBoolean(trackingPosition);
        }

        if (protocol < MINECRAFT_1_17 || trackingPosition) {
            ProtocolUtil.writeVarInt(byteBuf, iconData.length);

            for (IconData icon : iconData) {
                ProtocolUtil.writeVarInt(byteBuf, icon.type);
                byteBuf.writeByte(icon.x);
                byteBuf.writeByte(icon.z);
                byteBuf.writeByte(icon.direction);
                if (icon.displayName != null) {
                    byteBuf.writeBoolean(true);
                    ProtocolUtil.writeString(byteBuf, icon.displayName);
                } else {
                    byteBuf.writeBoolean(false);
                }
            }
        }

        byteBuf.writeByte(mapData.columns());

        if (mapData.columns() > 0) {
            byteBuf.writeByte(mapData.rows());
            byteBuf.writeByte(mapData.posX());
            byteBuf.writeByte(mapData.posZ());
            ProtocolUtil.writeByteArray(byteBuf, mapData.data());
        }

    }

    private record IconData(int type, byte x, byte z, byte direction, String displayName) {
    }
}
