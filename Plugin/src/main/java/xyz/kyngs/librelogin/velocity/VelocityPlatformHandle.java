/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.velocity;

import com.google.common.base.MoreObjects;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.server.ServerPing;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VelocityPlatformHandle implements PlatformHandle<Player, RegisteredServer> {
    private final VelocityLibreLogin plugin;

    public VelocityPlatformHandle(VelocityLibreLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Audience getAudienceForPlayer(Player player) {
        return player;
    }

    @Override
    public UUID getUUIDForPlayer(Player player) {
        return player.getUniqueId();
    }

    @Override
    public CompletableFuture<Throwable> movePlayer(Player player, RegisteredServer to) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = player.createConnectionRequest(to).connect().get();
                var reason = result.getReasonComponent();
                return result.isSuccessful() ? null : reason.map(component -> new RuntimeException("Failed to move player: " + Component.empty().append(component).content())).orElseGet(() -> new RuntimeException("Failed to move player"));
            } catch (InterruptedException ignored) {
                return null;
            } catch (ExecutionException e) {
                return e.getCause();
            }
        });
    }

    @Override
    public void kick(Player player, Component reason) {
        player.disconnect(reason);
    }

    @Override
    public RegisteredServer getServer(String name, boolean limbo) {
        Optional<RegisteredServer> serverOptional = plugin.getServer().getServer(name);
        if (serverOptional.isPresent())
            return serverOptional.get();
        if (limbo && plugin.getLimboIntegration() != null)
            return plugin.getLimboIntegration().createLimbo(name);
        return null;
    }

    @Override
    public Class<RegisteredServer> getServerClass() {
        return RegisteredServer.class;
    }

    @Override
    public Class<Player> getPlayerClass() {
        return Player.class;
    }

    @Override
    public String getIP(Player player) {
        return player.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public ServerPing ping(RegisteredServer server) {
        try {
            var players = server.ping().get().getPlayers();

            return players.map(value -> new ServerPing(value.getMax() == -1 ? Integer.MAX_VALUE : value.getMax())).orElse(null);

        } catch (InterruptedException | ExecutionException e) {
            plugin.getLogger().debug("Failed to ping server " + e.getMessage());
            return null;
        }
    }

    @Override
    public Collection<RegisteredServer> getServers() {
        return plugin.getServer().getAllServers();
    }

    @Override
    public String getServerName(RegisteredServer server) {
        return server.getServerInfo().getName();
    }

    @Override
    public int getConnectedPlayers(RegisteredServer server) {
        return server.getPlayersConnected().size();
    }

    @Override
    public String getPlayersServerName(Player player) {
        var server = player.getCurrentServer();

        return server.map(serverConnection -> serverConnection.getServerInfo().getName()).orElse(null);
    }

    @Override
    public String getPlayersVirtualHost(Player player) {
        var virt = player.getVirtualHost().orElse(null);

        return virt == null ? null : virt.getHostName();
    }

    @Override
    public String getUsernameForPlayer(Player player) {
        return player.getUsername();
    }

    @Override
    public String getPlatformIdentifier() {
        return "velocity";
    }

    @Override
    public ProxyData getProxyData() {
        return new ProxyData(
                plugin.getServer().getVersion().toString(),
                getServers().stream().map(Object::toString).toList(),
                plugin.getServer().getPluginManager().getPlugins().stream().map(plugin ->
                        MoreObjects.toStringHelper(plugin.getInstance().orElse(null))
                                .add("desc", plugin.getDescription().toString())
                                .toString()
                ).toList(),
                plugin.getServerHandler().getLimboServers().stream().map(Object::toString).toList(),
                plugin.getServerHandler().getLobbyServers().values().stream().map(Object::toString).toList()
        );
    }
}
