package xyz.kyngs.librelogin.common.server;

import xyz.kyngs.librelogin.api.server.ServerPing;
import xyz.kyngs.librelogin.api.server.ServerPinger;

public class DummyServerPinger<S> implements ServerPinger<S> {
    @Override
    public ServerPing getLatestPing(S server) {
        return new ServerPing(Integer.MAX_VALUE);
    }
}
