package xyz.kyngs.librepremium.common.config;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.common.config.migrate.messages.FirstMessagesMigrator;
import xyz.kyngs.librepremium.common.config.migrate.messages.SecondMessagesMigrator;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HoconMessages implements Messages {

    private final static LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final Map<String, TextComponent> messages;
    private final Logger logger;

    public HoconMessages(Logger logger) {
        this.logger = logger;
        messages = new HashMap<>();
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
    public void reload(LibrePremiumPlugin plugin) throws IOException, CorruptedConfigurationException {
        var adept = new ConfigurateConfiguration(
                plugin.getDataFolder(),
                "messages.conf",
                DefaultMessages.class,
                """
                          !!THIS FILE IS WRITTEN IN THE HOCON FORMAT!!
                          The hocon format is very similar to JSON, but it has some extra features.
                          You can find more information about the format on the sponge wiki:
                          https://docs.spongepowered.org/stable/en/server/getting-started/configuration/hocon.html
                          ----------------------------------------------------------------------------------------
                          LibrePremium Messages
                          ----------------------------------------------------------------------------------------
                          This file contains all of the messages used by the plugin, you are welcome to fit it to your needs.
                          You can find more information about LibrePremium on the github page:
                          https://github.com/kyngs/LibrePremium
                        """,
                logger,
                new FirstMessagesMigrator(),
                new SecondMessagesMigrator()
        );

        var node = adept.getHelper().configuration();

        node.childrenMap().forEach((key, value) -> {
                    if (!(key instanceof String str)) return;
                    var string = value.getString();

                    if (string == null) return;

                    messages.put(str, SERIALIZER.deserialize(string));
                }
        );
    }
}
