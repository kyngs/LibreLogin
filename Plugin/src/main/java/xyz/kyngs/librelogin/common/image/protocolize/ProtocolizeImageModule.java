/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.image.protocolize;

import dev.simplix.protocolize.api.PacketDirection;
import dev.simplix.protocolize.api.Protocol;
import dev.simplix.protocolize.api.module.ProtocolizeModule;
import dev.simplix.protocolize.api.providers.MappingProvider;
import dev.simplix.protocolize.api.providers.ProtocolRegistrationProvider;
import xyz.kyngs.librelogin.common.image.protocolize.packet.MapDataPacket;

public class ProtocolizeImageModule implements ProtocolizeModule {

    @Override
    public void registerMappings(MappingProvider mappingProvider) {

    }

    @Override
    public void registerPackets(ProtocolRegistrationProvider protocolRegistrationProvider) {
        protocolRegistrationProvider.registerPacket(MapDataPacket.MAPPINGS, Protocol.PLAY, PacketDirection.CLIENTBOUND, MapDataPacket.class);
    }

}
