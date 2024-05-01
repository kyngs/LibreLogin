/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api;

/**
 * Logger class for LibreLogin.
 *
 * @author kyngs
 */
public interface Logger {

    /**
     * Logs a message at the INFO level.
     *
     * @param message The message to log.
     */
    void info(String message);

    /**
     * Logs a message, and a throwable at the INFO level.
     *
     * @param message   The message to log.
     * @param throwable The throwable to log.
     */
    void info(String message, Throwable throwable);

    /**
     * Logs a message at the WARNING level.
     *
     * @param message The message to log.
     */
    void warn(String message);

    /**
     * Logs a message, and a throwable at the WARNING level.
     *
     * @param message   The message to log.
     * @param throwable The throwable to log.
     */
    void warn(String message, Throwable throwable);

    /**
     * Logs a message at the ERROR level.
     *
     * @param message The message to log.
     */
    void error(String message);

    /**
     * Logs a message, and a throwable at the ERROR level.
     *
     * @param message   The message to log.
     * @param throwable The throwable to log.
     */
    void error(String message, Throwable throwable);

    /**
     * Logs a message at the DEBUG level.
     *
     * @param message The message to log.
     */
    void debug(String message);

    /**
     * Logs a message, and a throwable at the DEBUG level.
     *
     * @param message   The message to log.
     * @param throwable The throwable to log.
     */
    void debug(String message, Throwable throwable);

}
