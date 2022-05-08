package xyz.kyngs.librepremium.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import xyz.kyngs.librepremium.api.authorization.AuthorizationProvider;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;

public class Blockers {

    private final AuthorizationProvider authorizationProvider;
    private final PluginConfiguration configuration;

    public Blockers(AuthorizationProvider authorizationProvider, PluginConfiguration configuration) {
        this.authorizationProvider = authorizationProvider;
        this.configuration = configuration;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChat(PlayerChatEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer().getUniqueId()) || authorizationProvider.isAwaiting2FA(event.getPlayer().getUniqueId()))
            event.setResult(PlayerChatEvent.ChatResult.denied());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;
        if (authorizationProvider.isAuthorized(player.getUniqueId()) && !authorizationProvider.isAwaiting2FA(player.getUniqueId()))
            return;

        var command = event.getCommand();

        for (String allowed : configuration.getAllowedCommandsWhileUnauthorized()) {
            if (command.startsWith(allowed)) return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerConnect(ServerPreConnectEvent event) {
        var id = event.getPlayer().getUniqueId();

        if (authorizationProvider.isAwaiting2FA(id)) {
            if (!configuration.getLimbo().contains(event.getOriginalServer().getServerInfo().getName())) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }
        }
    }

}
