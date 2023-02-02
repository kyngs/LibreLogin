package xyz.kyngs.librelogin.common;

import xyz.kyngs.librelogin.api.PlatformHandle;

public class AuthenticHandler<P, S> {

    protected final AuthenticLibreLogin<P, S> plugin;
    protected final PlatformHandle<P, S> platformHandle;

    public AuthenticHandler(AuthenticLibreLogin<P, S> plugin) {
        this.plugin = plugin;
        this.platformHandle = plugin.getPlatformHandle();
    }
}
