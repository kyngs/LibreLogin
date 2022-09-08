package xyz.kyngs.librepremium.bungeecord;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.kyngs.librepremium.api.PlatformHandle;
import xyz.kyngs.librepremium.api.server.ServerPing;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class BungeeCordPlatformHandle implements PlatformHandle<ProxiedPlayer, ServerInfo> {

    private final BungeeCordLibrePremium plugin;

    public BungeeCordPlatformHandle(BungeeCordLibrePremium plugin) {
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
    public ServerInfo getServer(String name) {
        return plugin.getBootstrap().getProxy().getServerInfo(name);
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


}
