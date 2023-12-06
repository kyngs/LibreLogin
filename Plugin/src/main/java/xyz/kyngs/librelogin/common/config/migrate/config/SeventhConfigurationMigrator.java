package xyz.kyngs.librelogin.common.config.migrate.config;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;
import xyz.kyngs.librelogin.common.config.migrate.ConfigurationMigrator;

public class SeventhConfigurationMigrator implements ConfigurationMigrator {

    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var kickOnWrongPassword = helper.getBoolean("kick-on-wrong-password");

        if (kickOnWrongPassword)
            helper.set("kick-on-wrong-password", 1);
        else
            helper.set("kick-on-wrong-password", 0);
    }

}
