package xyz.kyngs.librepremium.api.event.events;

import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.event.PlayerBasedEvent;

/**
 * Fires after player has changed their password, their profile is already updated at the moment the event fires
 *
 * @author kyngs
 */
public interface PasswordChangeEvent extends PlayerBasedEvent {

    /**
     * Gets the player's old password, in hashed form
     *
     * @return player's old password
     */
    HashedPassword getOldPassword();

}
