package xyz.kyngs.librelogin.common.config.migrate;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;

public interface ConfigurationMigrator {

    void migrate(ConfigurateHelper helper, Logger logger);

}
