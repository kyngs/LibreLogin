package xyz.kyngs.librepremium.api;

/**
 * Logger class for LibrePremium.
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
     * Logs a message at the WARNING level.
     *
     * @param message The message to log.
     */
    void warn(String message);

    /**
     * Logs a message at the ERROR level.
     *
     * @param message The message to log.
     */
    void error(String message);

    /**
     * Logs a message at the DEBUG level.
     *
     * @param message The message to log.
     */
    void debug(String message);

}
