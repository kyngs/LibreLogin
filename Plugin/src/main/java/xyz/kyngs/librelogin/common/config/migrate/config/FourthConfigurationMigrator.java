package xyz.kyngs.librelogin.common.config.migrate.config;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;
import xyz.kyngs.librelogin.common.config.migrate.ConfigurationMigrator;

public class FourthConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        helper.set("database.properties.mysql.host", helper.getString("database.host"));
        helper.set("database.properties.mysql.database", helper.getString("database.database"));
        helper.set("database.properties.mysql.password", helper.getString("database.password"));
        helper.set("database.properties.mysql.port", helper.getInt("database.port"));
        helper.set("database.properties.mysql.user", helper.getString("database.user"));
        helper.set("database.properties.mysql.max-life-time", helper.getInt("database.max-life-time"));
        helper.set("database.type", "librelogin-mysql");
        helper.set("migration", null);
    }
}
