package xyz.kyngs.librepremium.common.config.migrate.config;

import xyz.kyngs.librepremium.common.config.ConfigurateHelper;
import xyz.kyngs.librepremium.common.config.migrate.ConfigurationMigrator;

import java.util.List;

public class FirstConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper) {
        var limbo = helper.getString("limbo");

        helper.set("limbo", List.of(limbo));
    }
}
