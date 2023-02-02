package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.crypto.HashedPassword;
import xyz.kyngs.librelogin.api.event.PlayerBasedEvent;

/**
 * Fires after player has changed their password, their profile is already updated at the moment the event fires
 *
 * @author kyngs
 */
public interface PasswordChangeEvent<P, S> extends PlayerBasedEvent<P, S> {

    /**
     * Gets the player's old password, in hashed form
     *
     * @return player's old password
     */
    HashedPassword getOldPassword();

}
