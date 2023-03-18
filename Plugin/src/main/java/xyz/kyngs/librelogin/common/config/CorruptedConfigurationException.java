/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config;

/**
 * This exception is thrown when the configuration is corrupted.
 *
 * @author kyngs
 */
public class CorruptedConfigurationException extends Exception {
    public CorruptedConfigurationException(Throwable cause) {
        super(cause);
    }

    public CorruptedConfigurationException(String message) {
        super(message);
    }
}
