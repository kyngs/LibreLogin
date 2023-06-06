/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.database;

import java.util.Collection;
import java.util.UUID;

/**
 * This interface is used to read from the database.
 *
 * @author kyngs
 */
public interface ReadDatabaseProvider {

    /**
     * This method finds a player by their name.
     *
     * @param name The name of the player.
     * @return The player, or null if the player does not exist.
     */
    User getByName(String name);

    /**
     * This method finds a player by their UUID.
     *
     * @param uuid The UUID of the player.
     * @return The player, or null if the player does not exist.
     */
    User getByUUID(UUID uuid);

    /**
     * This method finds a player by their premium UUID.
     *
     * @param uuid The premium UUID of the player.
     * @return The player, or null if the player does not exist.
     */
    User getByPremiumUUID(UUID uuid);

    /**
     * This method fetches all players. <b>Use this with caution.</b>
     *
     * @return A collection of all players.
     */
    Collection<User> getAllUsers();

    /**
     * This method fetches all players which used the specified IP.
     *
     * @param ip IP address.
     * @return A collection of all players which used the specified IP.
     */
    Collection<User> getByIP(String ip);

}
