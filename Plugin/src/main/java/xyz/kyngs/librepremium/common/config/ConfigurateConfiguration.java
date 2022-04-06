package xyz.kyngs.librepremium.common.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;

import java.io.File;
import java.io.IOException;

public class ConfigurateConfiguration {

    private final ConfigurateHelper helper;
    private final boolean newlyCreated;
    private final HoconConfigurationLoader loader;

    public ConfigurateConfiguration(File dataFolder, String name, CommentedConfigurationNode defaultConfiguration) throws IOException, CorruptedConfigurationException {
        var file = new File(dataFolder, name);

        if (!file.exists()) {
            newlyCreated = true;
            if (!file.createNewFile()) throw new IOException("Could not create configuration file!");
        } else newlyCreated = false;

        var builder = HoconConfigurationLoader.builder()
                .defaultOptions(
                        ConfigurationOptions
                                .defaults()
                                .header(defaultConfiguration.comment())
                )
                .file(file)
                .emitComments(true)
                .prettyPrinting(true);


        loader = builder.build();

        try {
            helper = new ConfigurateHelper(loader.load()
                    .mergeFrom(defaultConfiguration));
        } catch (ConfigurateException e) {
            throw new CorruptedConfigurationException(e);
        }

        save();
    }

    public ConfigurateHelper getHelper() {
        return helper;
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }

    public void save() throws IOException {
        loader.save(helper.configuration());
    }


}
