package xyz.kyngs.librepremium.api.event;

import org.jetbrains.annotations.Nullable;

/**
 * An abstract event for events, that require server choosing
 *
 * @author kyngs
 */
public interface ServerChooseEvent extends PlayerBasedEvent {

    /**
     * Gets the server
     *
     * @return null, if default will be used
     */
    @Nullable
    String getServer();

    /**
     * Set the server
     *
     * @param server the server, if null, the default will be used
     */
    void setServer(@Nullable String server);

}
