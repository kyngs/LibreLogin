/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.database.User;

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
     * @return the player, or null if the affected player did not execute the change
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
