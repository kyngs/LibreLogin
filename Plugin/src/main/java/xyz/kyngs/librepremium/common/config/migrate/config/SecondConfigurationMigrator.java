package xyz.kyngs.librepremium.common.config.migrate.config;

import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.common.config.ConfigurateHelper;
import xyz.kyngs.librepremium.common.config.migrate.ConfigurationMigrator;

public class SecondConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var list = helper.getStringList("allowed-commands-while-unauthorized");
        list.add("2faconfirm");
        helper.set("allowed-commands-while-unauthorized", list);
    }
}
