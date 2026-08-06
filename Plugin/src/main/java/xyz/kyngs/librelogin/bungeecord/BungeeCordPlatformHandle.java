/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord;

import com.google.common.base.MoreObjects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.server.ServerPing;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class BungeeCordPlatformHandle implements PlatformHandle<ProxiedPlayer, ServerInfo> {

    private final BungeeCordLibreLogin plugin;

    public BungeeCordPlatformHandle(BungeeCordLibreLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Audience getAudienceForPlayer(ProxiedPlayer player) {
        return plugin.getAdventure().player(player);
    }

    @Override
    public UUID getUUIDForPlayer(ProxiedPlayer player) {
        return player.getUniqueId();
    }

    @Override
    public CompletableFuture<Throwable> movePlayer(ProxiedPlayer player, ServerInfo to) {
        return CompletableFuture.supplyAsync(() -> {
            var latch = new CountDownLatch(1);

            var ref = new Throwable[1];

            player.connect(to, (result, error) -> {
                ref[0] = result ? null : (error == null ? new RuntimeException("Failed to move player") : error);

                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                return null;
            }

            return ref[0];
        });
    }

    @Override
    public void kick(ProxiedPlayer player, Component reason) {
        player.disconnect(plugin.getSerializer().serialize(reason));
    }

    @Override
    public ServerInfo getServer(String name, boolean limbo) {
        ServerInfo serverInfo = plugin.getBootstrap().getProxy().getServerInfo(name);
        if (serverInfo != null)
            return serverInfo;
        if (limbo && plugin.getLimboIntegration() != null)
            return plugin.getLimboIntegration().createLimbo(name);
        return null;
    }

    @Override
    public Class<ServerInfo> getServerClass() {
        return ServerInfo.class;
    }

    @Override
    public Class<ProxiedPlayer> getPlayerClass() {
        return ProxiedPlayer.class;
    }

    @Override
    public String getIP(ProxiedPlayer player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    @Override
    public ServerPing ping(ServerInfo server) {
        // I hate this...
        var latch = new CountDownLatch(1);
        var ref = new ServerPing[1];

        server.ping((result, error) -> {
            ref[0] = error == null ? new ServerPing(result.getPlayers().getMax() == -1 ? Integer.MAX_VALUE : result.getPlayers().getMax()) : null;

            if (error != null) {
                plugin.getLogger().debug("Failed to ping server: " + error.getMessage());
            }

            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }

        return ref[0];
    }

    @Override
    public Collection<ServerInfo> getServers() {
        return plugin.getBootstrap().getProxy().getServers().values();
    }

    @Override
    public String getServerName(ServerInfo server) {
        return server.getName();
    }

    @Override
    public int getConnectedPlayers(ServerInfo server) {
        return server.getPlayers().size();
    }

    @Override
    public String getPlayersServerName(ProxiedPlayer player) {
        var server = player.getServer();
        return server == null ? null : server.getInfo().getName();
    }

    @Override
    public String getPlayersVirtualHost(ProxiedPlayer player) {
        var virt = player.getPendingConnection().getVirtualHost();

        return virt == null ? null : virt.getHostName();
    }

    @Override
    public String getUsernameForPlayer(ProxiedPlayer player) {
        return player.getName();
    }

    @Override
    public String getPlatformIdentifier() {
        return "bungeecord";
    }

    @Override
    public ProxyData getProxyData() {
        return new ProxyData(
                plugin.getBootstrap().getProxy().getName() + " " + plugin.getBootstrap().getProxy().getVersion(),
                getServers().stream().map(this::fromServer).toList(),
                plugin.getBootstrap().getProxy().getPluginManager().getPlugins().stream().map(plugin ->
                        MoreObjects.toStringHelper(plugin)
                                .add("name", plugin.getDescription().getName())
                                .add("version", plugin.getDescription().getVersion())
                                .add("author", plugin.getDescription().getAuthor())
                                .add("main", plugin.getDescription().getMain())
                                .toString()
                ).toList(),
                plugin.getServerHandler().getLimboServers().stream().map(this::fromServer).toList(),
                plugin.getServerHandler().getLobbyServers().values().stream().map(this::fromServer).toList()
        );
    }

    private String fromServer(ServerInfo server) {
        return MoreObjects.toStringHelper(server)
                .add("name", server.getName())
                .add("address", server.getAddress().getAddress().getHostAddress())
                .add("port", server.getAddress().getPort())
                .add("motd", server.getMotd())
                .add("restricted", server.isRestricted())
                .add("online", server.getPlayers().size())
                .add("max", server.getPlayers().size())
                .toString();
    }


}
