package xyz.kyngs.librepremium.api.event;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librepremium.api.database.User;

import java.util.UUID;

/**
 * An abstract event for events, that include player info
 *
 * @author kyngs
 */
public interface PlayerBasedEvent<P, S> extends Event<P, S> {

    /**
     * Gets the player's UUID
     *
     * @return the player's uuid
     */
    UUID getUUID();

    /**
     * Gets the player
     *
     * @return the player
     */
    @Nullable
    P getPlayer();

    /**
     * Gets the audience
     *
     * @return audience of the player, all messages, titles etc. sent to this audience should be sent to the player
     */
    Audience getAudience();

    /**
     * Gets the player's database profile
     *
     * @return player's database profile or null if the player comes from floodgate
     */
    @Nullable
    User getUser();

}
