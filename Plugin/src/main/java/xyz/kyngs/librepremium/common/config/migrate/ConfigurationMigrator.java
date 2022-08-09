package xyz.kyngs.librepremium.common.config.migrate;

import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.common.config.ConfigurateHelper;

public interface ConfigurationMigrator {

    void migrate(ConfigurateHelper helper, Logger logger);

}
