/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.integration;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recognizes players that have already been authenticated by Minekube Connect.
 * <p>
 * Connect terminates the player's connection at its own edge, performs the Mojang handshake there,
 * and then relays the player into the proxy over a local channel which it marks with the
 * {@code connect-player} attribute. There is no second Mojang session left for the proxy to verify,
 * so forcing online mode on such a connection makes the proxy send an encryption request that can
 * never be answered and the login never completes.
 * <p>
 * Reading the marker needs no compile time dependency on Connect: Netty interns attribute keys by
 * name, so the attribute is simply absent when Connect is not installed. This is the same approach
 * the listeners already use for Floodgate's {@code floodgate-player} attribute.
 * <p>
 * The channel is only reachable while the player is logging in, so the UUIDs recognized during
 * login are remembered here and dropped again when the player disconnects. That gives the rest of
 * the plugin a UUID based check with the same shape as {@link FloodgateIntegration#isFloodgateId}.
 */
public class ConnectIntegration {

    private static final AttributeKey<?> CONNECT_ATTR = AttributeKey.valueOf("connect-player");

    private final Set<UUID> players = ConcurrentHashMap.newKeySet();

    /**
     * Checks whether the given connection channel has been marked by Connect.
     *
     * @param channel the connection channel, may be null if it could not be read
     * @return whether the player behind the channel has already been authenticated by Connect
     */
    public static boolean isConnectChannel(Channel channel) {
        return channel != null && channel.hasAttr(CONNECT_ATTR) && channel.attr(CONNECT_ATTR).get() != null;
    }

    public void addPlayer(UUID uuid) {
        if (uuid != null) players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        if (uuid != null) players.remove(uuid);
    }

    public boolean isConnectId(UUID uuid) {
        return uuid != null && players.contains(uuid);
    }

}
