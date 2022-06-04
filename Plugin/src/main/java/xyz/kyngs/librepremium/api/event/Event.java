package xyz.kyngs.librepremium.api.event;

import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.PlatformHandle;

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
    LibrePremiumPlugin<P, S> getPlugin();

    PlatformHandle<P, S> getPlatformHandle();

}
