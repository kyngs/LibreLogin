package xyz.kyngs.librelogin.common.server;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import xyz.kyngs.librelogin.api.server.ServerPing;
import xyz.kyngs.librelogin.api.server.ServerPinger;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AuthenticServerPinger<S> implements ServerPinger<S> {
    private final LoadingCache<S, Optional<ServerPing>> pingCache;

    public AuthenticServerPinger(AuthenticLibreLogin<?, S> plugin) {
        this.pingCache = Caffeine.newBuilder()
                .refreshAfterWrite(10, TimeUnit.SECONDS)
                .build(server -> {
                    plugin.getLogger().debug("Pinging server " + server);
                    var ping = plugin.getPlatformHandle().ping(server);
                    plugin.getLogger().debug("Pinged server " + server + ": " + ping);

                    return Optional.ofNullable(ping);
                });

        var handle = plugin.getPlatformHandle();
        handle.getServers().parallelStream()
                .filter(server -> {
                    if (plugin.getConfiguration().rememberLastServer()) return true;

                    var name = handle.getServerName(server);

                    return plugin.getConfiguration().getLimbo().contains(name) || plugin.getConfiguration().getPassThrough().containsValue(name);
                })
                .forEach(this::getLatestPing);
    }

    @Override
    public ServerPing getLatestPing(S server) {
        return pingCache.get(server).orElse(null);
    }
}
