/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.configuration;

import net.kyori.adventure.text.TextComponent;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.common.config.CorruptedConfigurationException;

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
    void reload(LibreLoginPlugin<?, ?> plugin) throws IOException, CorruptedConfigurationException;

}
