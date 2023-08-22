/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.velocity;

import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;
import xyz.kyngs.librelogin.common.SLF4JLogger;
import xyz.kyngs.librelogin.common.util.DependencyUtil;

import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;

@Plugin(
        id = "librelogin",
        name = "LibreLogin",
        version = "@version@",
        authors = "kyngs",
        dependencies = {
                @Dependency(id = "floodgate", optional = true),
                @Dependency(id = "protocolize", optional = true),
                @Dependency(id = "redisbungee", optional = true),
                @Dependency(id = "nanolimbovelocity", optional = true)
        }
)
public class VelocityBootstrap {

    @Inject
    ProxyServer server;
    @Inject
    Logger logger;
    @Inject
    Injector injector;
    private VelocityLibreLogin libreLogin;

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        DependencyUtil.downloadDependencies(
                new SLF4JLogger(logger, () -> false),
                new VelocityLibraryManager<>(logger, Path.of("plugins", "librelogin"), server.getPluginManager(), this),
                List.of(),
                List.of()
        );
        libreLogin = new VelocityLibreLogin(this);

        injector.injectMembers(libreLogin);

        libreLogin.enable();

        server.getEventManager().register(this, new Blockers(libreLogin.getAuthorizationProvider(), libreLogin.getConfiguration(), libreLogin.getMessages()));
        server.getEventManager().register(this, new VelocityListeners(libreLogin));
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        libreLogin.disable();
    }
}
