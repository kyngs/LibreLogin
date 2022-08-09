package xyz.kyngs.librepremium.common.config.migrate.messages;

import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.common.config.ConfigurateHelper;
import xyz.kyngs.librepremium.common.config.DefaultMessages;
import xyz.kyngs.librepremium.common.config.migrate.ConfigurationMigrator;

public class SecondMessagesMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        logger.warn("Sorry, but I've needed to reset the totp-show-info message, because the process has significantly changed. Here is the original: " + helper.getString("totp-show-info"));

        helper.set("totp-show-info", DefaultMessages.TOTP_SHOW_INFO.defaultValue());
    }
}
