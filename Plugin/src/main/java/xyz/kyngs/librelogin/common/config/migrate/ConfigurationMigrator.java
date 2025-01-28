/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config.migrate;

import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;

public interface ConfigurationMigrator {

    void migrate(ConfigurateHelper helper, Logger logger);

    default <T> void rename(String from, ConfigurationKey<T> to, ConfigurateHelper helper) {
        helper.set(to, to.getter().apply(helper, from));
        helper.set(from, null);
    }

}
