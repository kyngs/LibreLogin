package xyz.kyngs.librepremium.common.config;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YamlMessages implements Messages {

    private final static LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final Map<String, TextComponent> messages;

    public YamlMessages() {
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
        var file = new File(plugin.getDataFolder(), "messages.yml");
        var original = plugin.getResourceAsStream("messages.yml");

        YamlPluginConfiguration.loadAndVerifyConf(file, original).getValues(true).forEach((key, value) -> messages.put(key, SERIALIZER.deserialize(value.toString())));
    }
}
