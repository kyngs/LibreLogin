/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import xyz.kyngs.librelogin.api.server.ServerPing;

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

    S getServer(String name, boolean limbo);

    Class<S> getServerClass();

    Class<P> getPlayerClass();

    String getIP(P player);

    ServerPing ping(S server);

    Collection<S> getServers();

    String getServerName(S server);

    int getConnectedPlayers(S server);

    String getPlayersServerName(P player);

    String getPlayersVirtualHost(P player);

    String getUsernameForPlayer(P player);
}
