package xyz.kyngs.librepremium.common.config;

import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import xyz.kyngs.librepremium.api.BiHolder;
import xyz.kyngs.librepremium.api.LibrePremiumPlugin;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

public class YamlPluginConfiguration implements PluginConfiguration {

    private YamlConfiguration configuration;

    static BiHolder<YamlConfiguration, Boolean> loadAndVerifyConf(File file, InputStream original) throws IOException, CorruptedConfigurationException {
        var virgin = false;
        if (!file.exists()) {
            Files.copy(original, file.toPath());
            virgin = true;
        }

        var conf = YamlConfiguration.loadConfiguration(original);

        try {
            conf.load(file);
        } catch (InvalidConfigurationException e) {
            throw new CorruptedConfigurationException(e);
        }

        conf.save(file);

        return new BiHolder<>(conf, virgin);
    }

    @Override
    public boolean reload(LibrePremiumPlugin plugin) throws IOException, CorruptedConfigurationException {
        var file = new File(plugin.getDataFolder(), "config.yml");
        var original = plugin.getResourceAsStream("config.yml");

        var holder = loadAndVerifyConf(file, original);
        var adept = holder.key();

        if (plugin.getCryptoProvider(adept.getString("default-crypto-provider")) == null)
            throw new CorruptedConfigurationException("Crypto provider does not exist");

        configuration = adept;
        return holder.value();
    }

    @Override
    public List<String> getAllowedCommandsWhileUnauthorized() {
        return configuration.getStringList("allowed-commands-while-unauthorized");
    }

    @Override
    public String getDatabasePassword() {
        return configuration.getString("database.password");
    }

    @Override
    public String getDatabaseUsername() {
        return configuration.getString("database.username");
    }

    @Override
    public String getHost() {
        return configuration.getString("database.host");
    }

    @Override
    public String getDatabase() {
        return configuration.getString("database.database");
    }

    @Override
    public Collection<String> getPassThroughServers() {
        return configuration.getStringList("pass_through");
    }

    @Override
    public String getLimboServer() {
        return configuration.getString("limbo");
    }

    @Override
    public int getPort() {
        return configuration.getInt("database.port");
    }

    @Override
    public String getDefaultCryptoProvider() {
        return configuration.getString("default-crypto-provider");
    }

    @Override
    public boolean kickOnWrongPassword() {
        return configuration.getBoolean("kick-on-wrong-password");
    }

    @Override
    public boolean migrateOnNextStartup() {
        return configuration.getBoolean("migration.on-next-startup");
    }

    @Override
    public String getMigrator() {
        return configuration.getString("migration.type");
    }

    @Override
    public String getOldDatabaseHost() {
        return configuration.getString("migration.old-database.host");
    }

    @Override
    public int getOldDatabasePort() {
        return configuration.getInt("migration.old-database.port");
    }

    @Override
    public String getOldDatabaseUsername() {
        return configuration.getString("migration.old-database.username");
    }

    @Override
    public String getOldDatabasePassword() {
        return configuration.getString("migration.old-database.password");
    }

    @Override
    public String getOldDatabase() {
        return configuration.getString("migration.old-database.database");
    }

    @Override
    public String getOldTable() {
        return configuration.getString("migration.old-database.table");
    }
}
