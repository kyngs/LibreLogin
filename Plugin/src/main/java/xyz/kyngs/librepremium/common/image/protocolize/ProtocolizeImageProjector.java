package xyz.kyngs.librepremium.common.image.protocolize;

import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.api.providers.ModuleProvider;
import dev.simplix.protocolize.api.util.ProtocolVersions;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.packets.HeldItemChange;
import dev.simplix.protocolize.data.packets.SetSlot;
import xyz.kyngs.librepremium.api.image.ImageProjector;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.image.AuthenticImageProjector;
import xyz.kyngs.librepremium.common.image.protocolize.packet.MapDataPacket;

import java.awt.image.BufferedImage;

public class ProtocolizeImageProjector<P, S> extends AuthenticImageProjector<P, S> implements ImageProjector<P> {

    public ProtocolizeImageProjector(AuthenticLibrePremium<P, S> plugin) {
        super(plugin);
    }

    public boolean compatible() {
        return !Protocolize.version().equals("2.2.2");
    }

    @Override
    public void enable() {
        Protocolize.getService(ModuleProvider.class).registerModule(new ProtocolizeImageModule());
    }

    /**
     * <b>This implementation only really renders pure black and everything else as transparent. Shouldn't be used for anything else than a QR code.</b>
     *
     * @param image  The image to render.
     * @param player The player to render the image to.
     */
    @Override
    public void project(BufferedImage image, P player) {
        var id = platformHandle.getUUIDForPlayer(player);

        var protocolize = Protocolize.playerProvider().player(id);
        var protocol = protocolize.protocolVersion();
        var item = new ItemStack(
                ItemType.FILLED_MAP,
                1,
                (short) 0
        );

        if (protocol >= ProtocolVersions.MINECRAFT_1_17) {
            item.nbtData()
                    .putInt("map", 0);
        }

        protocolize.sendPacket(
                new SetSlot()
                        .slot((short) 36)
                        .itemStack(item)
        );

        protocolize.sendPacketToServer(
                new HeldItemChange()
                        .newSlot((short) 0)
        );

        protocolize.sendPacket(
                new HeldItemChange()
                        .newSlot((short) 0)
        );

        if (image.getWidth() != 128 && image.getHeight() != 128) {
            var resized = new BufferedImage(128, 128, image.getType());

            var graphics = resized.createGraphics();
            graphics.drawImage(image, 0, 0, 128, 128, 0, 0, image.getWidth(), image.getHeight(), null);
            graphics.dispose();

            image = resized;
        }

        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        byte[] data = new byte[16384];

        for (int i = 0; i < pixels.length; i++) {
            data[i] = (byte) (pixels[i] == -16777216 ? 116 : 56);
        }

        protocolize.sendPacket(new MapDataPacket(0, (byte) 0, new MapData(128, 128, 0, 0, data)));
    }

    @Override
    public boolean canProject(P player) {
        var id = platformHandle.getUUIDForPlayer(player);

        var protocolize = Protocolize.playerProvider().player(id);

        return protocolize.protocolVersion() >= ProtocolVersions.MINECRAFT_1_13 && protocolize.protocolVersion() <= ProtocolVersions.MINECRAFT_1_19;
    }

}
