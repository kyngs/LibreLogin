package xyz.kyngs.librepremium.api.event;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.database.User;

import java.util.UUID;

/**
 * An abstract event for events, that include player info
 *
 * @author kyngs
 */
public interface PlayerBasedEvent extends Event {

    /**
     * Gets the player's UUID
     *
     * @return the player's uuid
     */
    UUID getUUID();

    /**
     * Gets the audience
     *
     * @return audience of the player, all messages, titles etc. sent to this audience should be sent to the player
     */
    Audience getAudience();

    /**
     * Gets the player's database profile
     *
     * @return player's database profile - it's uuid should match {@link #getUUID()}
     */
    User getUser();

}
