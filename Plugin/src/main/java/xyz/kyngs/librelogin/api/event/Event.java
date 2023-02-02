package xyz.kyngs.librelogin.api.event;

import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.PlatformHandle;

/**
 * An abstract event for all events
 *
 * @author kyngs
 */
public interface Event<P, S> {

    /**
     * Gets the plugin instance
     *
     * @return the plugin instance
     */
    LibreLoginPlugin<P, S> getPlugin();

    PlatformHandle<P, S> getPlatformHandle();

}
