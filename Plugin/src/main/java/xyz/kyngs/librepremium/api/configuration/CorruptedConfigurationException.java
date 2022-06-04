package xyz.kyngs.librepremium.api.configuration;

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
