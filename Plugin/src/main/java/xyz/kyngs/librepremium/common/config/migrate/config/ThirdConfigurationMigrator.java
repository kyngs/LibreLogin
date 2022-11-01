package xyz.kyngs.librepremium.common.config.migrate.config;

import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.common.config.ConfigurateHelper;
import xyz.kyngs.librepremium.common.config.migrate.ConfigurationMigrator;

public class ThirdConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var list = helper.getStringList("pass-through");

        helper.set("pass-through", null);
        helper.set("pass-through.root", list);
    }
}
