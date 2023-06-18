/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.CommentedConfigurationNode;
import xyz.kyngs.librelogin.api.BiHolder;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librelogin.api.configuration.Messages;
import xyz.kyngs.librelogin.common.config.migrate.messages.FirstMessagesMigrator;
import xyz.kyngs.librelogin.common.config.migrate.messages.SecondMessagesMigrator;
import xyz.kyngs.librelogin.common.config.migrate.messages.ThirdMessagesMigrator;
import xyz.kyngs.librelogin.common.util.GeneralUtil;
import xyz.kyngs.utils.legacymessage.LegacyMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HoconMessages implements Messages {

    private static final MiniMessage SERIALIZER = MiniMessage.builder()
            .build();
    private final Map<String, TextComponent> messages;
    private final Logger logger;

    public HoconMessages(Logger logger) {
        this.logger = logger;
        messages = new HashMap<>();
    }

    public Map<String, TextComponent> getMessages() {
        return messages;
    }

    @Override
    public TextComponent getMessage(String key, String... replacements) {

        var message = messages.get(key);

        if (replacements.length == 0) return message;

        var replaceMap = new HashMap<String, String>();

        String toReplace = null;

        for (int i = 0; i < replacements.length; i++) {
            if (i % 2 != 0) {
                replaceMap.put(toReplace, replacements[i]);
            } else {
                toReplace = replacements[i];
            }
        }

        return GeneralUtil.formatComponent(message, replaceMap);
    }

    @Override
    public void reload(LibreLoginPlugin<?, ?> plugin) throws IOException, CorruptedConfigurationException {
        var adept = new ConfigurateConfiguration(
                plugin.getDataFolder(),
                "messages.conf",
                Set.of(new BiHolder<>(MessageKeys.class, "")),
                """
                          !!THIS FILE IS WRITTEN IN THE HOCON FORMAT!!
                          The hocon format is very similar to JSON, but it has some extra features.
                          You can find more information about the format on the sponge wiki:
                          https://docs.spongepowered.org/stable/en/server/getting-started/configuration/hocon.html
                          ----------------------------------------------------------------------------------------
                          LibreLogin Messages
                          ----------------------------------------------------------------------------------------
                          This file contains all of the messages used by the plugin, you are welcome to fit it to your needs.
                          The messages can be written both in the legacy format and in the MiniMessage format. For example, the following message is completely valid: <bold>&aReloaded!</bold>
                          You can find more information about LibreLogin on the github page:
                          https://github.com/kyngs/LibreLogin
                        """,
                logger,
                new FirstMessagesMigrator(),
                new SecondMessagesMigrator(),
                new ThirdMessagesMigrator()
        );

        extractKeys("", adept.getHelper().configuration());
    }

    private void extractKeys(String prefix, CommentedConfigurationNode node) {
        node.childrenMap().forEach((key, value) -> {
            if (!(key instanceof String str)) return;

            if (value.childrenMap().isEmpty()) {
                var string = value.getString();

                if (string == null) return;

                messages.put(prefix + str, Component.empty().append(SERIALIZER.deserialize(LegacyMessage.fromLegacy(string, "&"))));
            } else {
                extractKeys(prefix + str + ".", value);
            }
        });
    }
}
