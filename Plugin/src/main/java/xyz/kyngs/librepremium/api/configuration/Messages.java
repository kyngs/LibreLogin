package xyz.kyngs.librepremium.api.configuration;

import net.kyori.adventure.text.TextComponent;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;

import java.io.IOException;

public interface Messages {

    TextComponent getMessage(String key, String... replacements);

    void reload(LibrePremiumPlugin plugin) throws IOException, CorruptedConfigurationException;

}
