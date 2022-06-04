package xyz.kyngs.librepremium.api.configuration;

import net.kyori.adventure.text.TextComponent;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;

import java.io.IOException;

/**
 * This interface manages the messages of the plugin.
 *
 * @author kyngs
 */
public interface Messages {

    /**
     * Gets the message with the given key.
     *
     * @param key          The message key.
     * @param replacements Allows you to replace the placeholders in the message.
     * @return The message, or null if the message does not exist.
     */
    TextComponent getMessage(String key, String... replacements);

    /**
     * Reloads the messages.
     *
     * @param plugin The plugin.
     * @throws IOException                     If an I/O error occurs.
     * @throws CorruptedConfigurationException If the configuration is corrupted.
     */
    void reload(LibrePremiumPlugin<?, ?> plugin) throws IOException, CorruptedConfigurationException;

}
