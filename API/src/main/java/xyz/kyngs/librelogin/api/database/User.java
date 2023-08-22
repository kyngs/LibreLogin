/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.database;

import xyz.kyngs.librelogin.api.crypto.HashedPassword;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * A user in the database.
 *
 * @author kyngs
 */
public interface User {

    /**
     * Retrieves the TOTP secret string.
     *
     * @return the secret string
     */
    String getSecret();

    /**
     * Sets the TOTP secret string.
     *
     * @param secret the secret string to be set
     */
    void setSecret(String secret);

    /**
     * Retrieves the IP address of the current user.
     *
     * @return the IP address of the current user as a string
     */
    String getIp();

    /**
     * Sets the IP address of the current user.
     *
     * @param ip the IP address to set for the current user
     */
    void setIp(String ip);

    /**
     * Retrieves the IP address of the last server the current user connected to.
     *
     * @return the IP address of the last server
     */
    String getLastServer();

    /**
     * Sets the IP address of the last server the current user connected to.
     *
     * @param lastServer the IP address of the last server
     */
    void setLastServer(String lastServer);

    /**
     * Returns the timestamp of the last authentication of the current user.
     *
     * @return the timestamp of the last authentication
     */
    Timestamp getLastAuthentication();

    /**
     * Sets the timestamp of the last authentication of the current user.
     *
     * @param lastAuthentication the timestamp of the last authentication
     */
    void setLastAuthentication(Timestamp lastAuthentication);

    /**
     * Returns the timestamp of the join date of the current user.
     *
     * @return the timestamp of the join date
     */
    Timestamp getJoinDate();

    /**
     * Sets the join date of the current user.
     *
     * @param joinDate the timestamp representing the join date
     */
    void setJoinDate(Timestamp joinDate);

    /**
     * Returns the timestamp representing the last seen date of the current user.
     *
     * @return the timestamp representing the last seen date
     */
    Timestamp getLastSeen();

    /**
     * Sets the timestamp representing the last seen date of the current user.
     *
     * @param lastSeen the timestamp representing the last seen date
     */
    void setLastSeen(Timestamp lastSeen);

    /**
     * Retrieves the hashed password for the current user.
     *
     * @return the hashed password as a HashedPassword object
     */
    HashedPassword getHashedPassword();

    /**
     * Sets the hashed password for the current user.
     *
     * @param hashedPassword the hashed password to be set as a HashedPassword object
     */
    void setHashedPassword(HashedPassword hashedPassword);

    /**
     * Returns the UUID (Universally Unique Identifier) of the current user.
     *
     * @return the UUID of the current user as a UUID object
     */
    UUID getUuid();

    /**
     * Returns the premium UUID (Universally Unique Identifier) of the current user.
     *
     * @return the premium UUID of the current user as a UUID object
     */
    UUID getPremiumUUID();

    /**
     * Sets the premium UUID (Universally Unique Identifier) of the current user.
     *
     * @param premiumUUID the premium UUID to set for the current user as a UUID object
     */
    void setPremiumUUID(UUID premiumUUID);

    /**
     * Returns the last nickname of the current user.
     *
     * @return the last nickname of the current user as a String
     */
    String getLastNickname();

    /**
     * Sets the last nickname of the current user.
     *
     * @param lastNickname the last nickname to be set for the current user
     */
    void setLastNickname(String lastNickname);

    /**
     * Checks if the current user is registered.
     *
     * @return true if the user is registered, false otherwise
     */
    boolean isRegistered();

    /**
     * Checks if auto login is enabled for the current user.
     *
     * @return true if auto login is enabled, false otherwise
     */
    boolean autoLoginEnabled();

    /**
     * Returns the email address of the current user.
     *
     * @return the email address as a string
     */
    String getEmail();

    /**
     * Sets the email address for the current user.
     *
     * @param email the email address to be set
     */
    void setEmail(String email);

}
