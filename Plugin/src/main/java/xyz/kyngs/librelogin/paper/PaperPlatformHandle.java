package xyz.kyngs.librelogin.paper;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.kyngs.librelogin.api.PlatformHandle;
import xyz.kyngs.librelogin.api.server.ServerPing;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperPlatformHandle implements PlatformHandle<Player, World> {

    private final PaperLibreLogin plugin;

    public PaperPlatformHandle(PaperLibreLogin plugin) {
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
    public CompletableFuture<Throwable> movePlayer(Player player, World to) {
        return player.teleportAsync(to.getSpawnLocation())
                .thenApply(success -> success ? null : new RuntimeException("Unknown cause"));
    }

    @Override
    public void kick(Player player, Component reason) {
        PaperUtil.runSyncAndWait(() -> player.kick(reason), plugin);
    }

    @Override
    public World getServer(String name) {
        return Bukkit.getWorld(name);
    }

    @Override
    public Class<World> getServerClass() {
        return World.class;
    }

    @Override
    public Class<Player> getPlayerClass() {
        return Player.class;
    }

    @Override
    public String getIP(Player player) {
        return player.getAddress().getHostName();
    }

    @Override
    public ServerPing ping(World server) {
        return new ServerPing(Integer.MAX_VALUE);
    }

    @Override
    public Collection<World> getServers() {
        return Bukkit.getWorlds();
    }

    @Override
    public String getServerName(World server) {
        return server.getName();
    }

    @Override
    public int getConnectedPlayers(World server) {
        return server.getPlayerCount();
    }

    @Override
    public String getPlayersServerName(Player player) {
        return player.getWorld().getName();
    }
}
