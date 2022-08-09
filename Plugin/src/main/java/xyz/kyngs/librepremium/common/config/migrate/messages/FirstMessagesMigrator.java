package xyz.kyngs.librepremium.common.config.migrate.messages;

import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.common.config.ConfigurateHelper;
import xyz.kyngs.librepremium.common.config.migrate.ConfigurationMigrator;

public class FirstMessagesMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var autoLoginText = helper.getString("info-automatically-logged-in");

        if (autoLoginText != null) {
            helper.set("info-premium-logged-in", autoLoginText);
            helper.set("info-session-logged-in", autoLoginText);
        }

        helper.set("info-automatically-logged-in", null);
    }
}
