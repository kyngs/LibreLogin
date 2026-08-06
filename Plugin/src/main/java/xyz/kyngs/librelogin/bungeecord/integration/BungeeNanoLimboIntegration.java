/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord.integration;

import java.net.SocketAddress;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.data.InfoForwarding;
import xyz.kyngs.librelogin.common.integration.nanolimbo.NanoLimboIntegration;

public class BungeeNanoLimboIntegration extends NanoLimboIntegration<ServerInfo> {

    private final ClassLoader classLoader;

    public BungeeNanoLimboIntegration(ClassLoader classLoader, String portRange) {
        super(portRange);
        this.classLoader = classLoader;
    }

    @Override
    public ServerInfo createLimbo(String serverName) {
        SocketAddress address = findLocalAvailableAddress().orElseThrow(() -> new IllegalStateException("Cannot find available port for limbo server!"));
        LimboServer server = createLimboServer(address);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(serverName, address, "", false);
        ProxyServer.getInstance().getConfig().getServers().put(serverInfo.getName(), serverInfo);
        return serverInfo;
    }

    @Override
    protected InfoForwarding createForwarding() {
        return FORWARDING_FACTORY.legacy();
    }

    @Override
    protected ClassLoader classLoader() {
        return classLoader;
    }

}
