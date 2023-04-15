/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import xyz.kyngs.librelogin.paper.PaperLibreLogin;
import xyz.kyngs.librelogin.paper.PaperListeners;

public class ProtocolListener extends PacketAdapter {

    private final PaperListeners delegate;

    public ProtocolListener(PaperListeners delegate, PaperLibreLogin plugin) {
        super(params()
                .plugin(plugin.getBootstrap())
                .types(PacketType.Login.Client.START, PacketType.Login.Client.ENCRYPTION_BEGIN)
                .optionAsync()
        );
        this.delegate = delegate;

        ProtocolLibrary.getProtocolManager()
                .getAsynchronousManager()
                .registerAsyncHandler(this)
                .start();
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {
        if (packetEvent.isCancelled()) return;

        delegate.onPacketReceive(packetEvent);
    }


}
