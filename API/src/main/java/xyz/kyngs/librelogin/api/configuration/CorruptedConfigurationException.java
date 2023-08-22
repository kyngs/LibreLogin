/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.configuration;

/**
 * This exception is thrown when the configuration is corrupted.
 *
 * @author kyngs
 */
public class CorruptedConfigurationException extends Exception {

    /**
     * Creates a new instance of CorruptedConfigurationException with the specified cause.
     *
     * @param cause the {@link Throwable} cause of the exception
     */
    public CorruptedConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance of CorruptedConfigurationException with the specified detail message.
     *
     * @param message the detail message
     */
    public CorruptedConfigurationException(String message) {
        super(message);
    }
}
