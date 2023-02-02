package xyz.kyngs.librelogin.api.event;

import org.jetbrains.annotations.Nullable;

/**
 * An abstract event for events, that require server choosing
 *
 * @author kyngs
 */
public interface ServerChooseEvent<P, S> extends PlayerBasedEvent<P, S> {

    /**
     * Gets the server
     *
     * @return null, if default will be used
     */
    @Nullable
    S getServer();

    /**
     * Set the server
     *
     * @param server the server, if null, the default will be used
     */
    void setServer(@Nullable S server);

}
