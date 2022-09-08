package xyz.kyngs.librepremium.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import xyz.kyngs.librepremium.api.PlatformHandle;
import xyz.kyngs.librepremium.api.server.ServerPing;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VelocityPlatformHandle implements PlatformHandle<Player, RegisteredServer> {
    private final VelocityLibrePremium plugin;

    public VelocityPlatformHandle(VelocityLibrePremium plugin) {
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
                return result.isSuccessful() ? null : reason.isEmpty() ? new RuntimeException("Failed to move player") : new RuntimeException("Failed to move player: " + Component.empty().append(reason.get()).content());
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
    public RegisteredServer getServer(String name) {
        return plugin.getServer().getServer(name).orElse(null);
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

            if (players.isEmpty()) {
                return null;
            }

            return new ServerPing(players.get().getMax() == -1 ? Integer.MAX_VALUE : players.get().getMax());
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

        return server.isEmpty() ? null : server.get().getServerInfo().getName();
    }
}
