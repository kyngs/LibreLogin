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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Platform-specific things that are used to interact with platform's objects
 *
 * @param <P> Player Type
 * @param <S> Server Type
 */
public interface PlatformHandle<P, S> {

    /**
     * Retrieves the audience associated with the given player.
     *
     * @param player the player for which to retrieve the audience
     * @return the audience associated with the given player
     */
    Audience getAudienceForPlayer(P player);

    /**
     * Retrieves the UUID associated with the given player.
     *
     * @param player the player for which to retrieve the UUID
     * @return the UUID associated with the given player
     */
    UUID getUUIDForPlayer(P player);

    /**
     * Moves the player to the specified location asynchronously and returns a CompletableFuture
     * that will be completed with an exception if an error occurs during the move.
     *
     * @param player the player to be moved
     * @param to     the location to which the player should be moved
     * @return a CompletableFuture that will be completed with an exception if an error occurs during the move
     */
    CompletableFuture<Throwable> movePlayer(P player, S to);

    /**
     * Kicks the player from the server with the specified reason.
     *
     * @param player the player to be kicked
     * @param reason the reason for kicking the player
     */
    void kick(P player, Component reason);

    /**
     * Retrieves the server with the specified name.
     *
     * @param name  the name of the server to retrieve
     * @param limbo a boolean indicating whether to retrieve servers in limbo status or not
     * @return the server with the specified name, or null if no server is found
     */
    S getServer(String name, boolean limbo);

    /**
     * Retrieves the class of the {@link S} type
     *
     * @return the class of the {@link S} type
     */
    Class<S> getServerClass();

    /**
     * Retrieves the class of the {@link P} type
     *
     * @return the class of the {@link P} type
     */
    Class<P> getPlayerClass();

    /**
     * Retrieves the IP address of the specified player.
     *
     * @param player the player object for which to retrieve the IP address
     * @return the IP address of the player as a string
     */
    String getIP(P player);

    /**
     * Pings the specified server to retrieve server information.
     *
     * @param server the server object to be pinged
     * @return the ServerPing object containing server information
     */
    ServerPing ping(S server);

    /**
     * Retrieves the collection of all servers.
     *
     * @return the collection of all servers
     */
    Collection<S> getServers();

    /**
     * Retrieves the name of the given server.
     *
     * @param server the server for which to retrieve the name
     * @return the name of the given server
     */
    String getServerName(S server);

    /**
     * Retrieves the number of connected players on the given server.
     *
     * @param server the server for which to retrieve the number of connected players
     * @return the number of connected players on the given server
     */
    int getConnectedPlayers(S server);

    /**
     * Retrieves the server name for the given player.
     *
     * @param player the player for which to retrieve the server name
     * @return the server name associated with the given player
     */
    String getPlayersServerName(P player);

    /**
     * Retrieves the virtual host for the given player.
     *
     * @param player the player for which to retrieve the virtual host
     * @return the virtual host associated with the given player
     */
    String getPlayersVirtualHost(P player);

    /**
     * Retrieves the username for the given player.
     *
     * @param player the player for which to retrieve the username
     * @return the username associated with the given player
     */
    String getUsernameForPlayer(P player);

    /**
     * Retrieves the platform identifier.
     *
     * @return the platform identifier
     */
    String getPlatformIdentifier();

    /**
     * Retrieves the proxy data.
     *
     * @return the proxy data
     */
    ProxyData getProxyData();

    /**
     * Represents the data for a proxy server.
     *
     * @param name    the name of the proxy server
     * @param servers the list of servers on the proxy
     * @param plugins the list of plugins on the proxy
     * @param limbos  the list of servers in limbo status on the proxy
     * @param lobbies the list of servers in lobby status on the proxy
     */
    record ProxyData(String name, List<String> servers, List<String> plugins, List<String> limbos,
                     List<String> lobbies) {
    }
}
