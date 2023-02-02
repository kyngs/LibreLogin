package xyz.kyngs.librelogin.bungeecord;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.kyngs.librelogin.api.provider.LibrePremiumProvider;

public class BungeeCordBootstrap extends Plugin implements LibrePremiumProvider<ProxiedPlayer, ServerInfo> {

    private BungeeCordLibrePremium librePremium;

    @Override
    public void onEnable() {
        librePremium = new BungeeCordLibrePremium(this);
        librePremium.enable();
    }

    @Override
    public void onDisable() {
        librePremium.disable();
    }

    @Override
    public BungeeCordLibrePremium getLibrePremium() {
        return librePremium;
    }

}
