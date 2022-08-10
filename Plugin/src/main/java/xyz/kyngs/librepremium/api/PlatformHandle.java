package xyz.kyngs.librepremium.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import xyz.kyngs.librepremium.api.server.ServerPing;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Platform-specific things that are used to interact with platform's objects
 *
 * @param <P> Player Type
 * @param <S> Server Type
 */
public interface PlatformHandle<P, S> {

    Audience getAudienceForPlayer(P player);

    UUID getUUIDForPlayer(P player);

    CompletableFuture<Throwable> movePlayer(P player, S to);

    void kick(P player, Component reason);

    S getServer(String name);

    Class<S> getServerClass();

    Class<P> getPlayerClass();

    String getIP(P player);

    ServerPing ping(S server);

    Collection<S> getServers();

    String getServerName(S server);

    int getConnectedPlayers(S server);

    String getPlayersServerName(P player);

}
