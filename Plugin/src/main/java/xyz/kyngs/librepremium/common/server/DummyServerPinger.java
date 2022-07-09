package xyz.kyngs.librepremium.common.server;

import xyz.kyngs.librepremium.api.server.ServerPing;
import xyz.kyngs.librepremium.api.server.ServerPinger;

public class DummyServerPinger<S> implements ServerPinger<S> {
    @Override
    public ServerPing getLatestPing(S server) {
        return new ServerPing(Integer.MAX_VALUE);
    }
}
