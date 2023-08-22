/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.bungeecord;

import net.byteflux.libby.BungeeLibraryManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.kyngs.librelogin.api.provider.LibreLoginProvider;
import xyz.kyngs.librelogin.common.util.DependencyUtil;

import java.util.List;

public class BungeeCordBootstrap extends Plugin implements LibreLoginProvider<ProxiedPlayer, ServerInfo> {

    private BungeeCordLibreLogin libreLogin;

    @Override
    public void onLoad() {
        DependencyUtil.downloadDependencies(
                new BungeeCordLogger(this, () -> false),
                new BungeeLibraryManager(this),
                List.of(),
                List.of()
        );

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
