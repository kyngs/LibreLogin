package xyz.kyngs.librelogin.bungeecord;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.kyngs.librelogin.api.provider.LibreLoginProvider;

public class BungeeCordBootstrap extends Plugin implements LibreLoginProvider<ProxiedPlayer, ServerInfo> {

    private BungeeCordLibreLogin libreLogin;

    @Override
    public void onLoad() {
        libreLogin = new BungeeCordLibreLogin(this);
    }

    @Override
    public void onEnable() {
        libreLogin.enable();
    }

    @Override
    public void onDisable() {
        libreLogin.disable();
    }

    @Override
    public BungeeCordLibreLogin getLibreLogin() {
        return libreLogin;
    }

}
