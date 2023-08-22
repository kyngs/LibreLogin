/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.integration.nanolimbo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.Optional;

import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.data.InfoForwarding;
import xyz.kyngs.librelogin.api.integration.LimboIntegration;

public abstract class NanoLimboIntegration<S> implements LimboIntegration<S> {

    protected static final InfoForwardingFactory FORWARDING_FACTORY = new InfoForwardingFactory();
    private final int portMin, portMax;

    public NanoLimboIntegration(String portRange) {
        this.portMin = Integer.parseInt(portRange.split("-")[0]);
        this.portMax = Integer.parseInt(portRange.split("-")[1]);
    }

    protected LimboServer createLimboServer(SocketAddress address) {
        return new LimboServer(new NanoLimboConfig(address, createForwarding()), new DummyCommandHandler(), classLoader());
    }

    protected abstract InfoForwarding createForwarding();

    protected abstract ClassLoader classLoader();

    protected Optional<InetSocketAddress> findLocalAvailableAddress() {
        for (int port = portMin; port <= portMax; port++) {
            try (ServerSocket ignored = new ServerSocket(port)) {
                return Optional.of(new InetSocketAddress(port));
            } catch (IOException ignored) {
            }
        }
        return Optional.empty();
    }


}
