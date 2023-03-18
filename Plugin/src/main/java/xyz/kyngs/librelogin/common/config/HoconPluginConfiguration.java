/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config;

import xyz.kyngs.librelogin.api.BiHolder;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;
import xyz.kyngs.librelogin.common.config.migrate.config.FirstConfigurationMigrator;
import xyz.kyngs.librelogin.common.config.migrate.config.FourthConfigurationMigrator;
import xyz.kyngs.librelogin.common.config.migrate.config.SecondConfigurationMigrator;
import xyz.kyngs.librelogin.common.config.migrate.config.ThirdConfigurationMigrator;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.*;

public class HoconPluginConfiguration {

    private final Logger logger;
    private final Collection<BiHolder<Class<?>, String>> defaultKeys;
    private ConfigurateHelper helper;
    private Duration sessionTimeout;

    public HoconPluginConfiguration(Logger logger, Collection<BiHolder<Class<?>, String>> defaultKeys) {
        this.logger = logger;
        this.defaultKeys = new ArrayList<>(defaultKeys); //Make this independent on the original collection in case it gets modified
        this.defaultKeys.add(new BiHolder<>(ConfigurationKeys.class, "")); //Make sure the default configuration keys always have top priority
    }

    public ConfigurateHelper getHelper() {
        return helper;
    }

    public boolean reload(LibreLoginPlugin<?, ?> plugin) throws IOException, CorruptedConfigurationException {
        var adept = new ConfigurateConfiguration(
                plugin.getDataFolder(),
                "config.conf",
                defaultKeys,
                """
                          !!THIS FILE IS WRITTEN IN THE HOCON FORMAT!!
                          The hocon format is very similar to JSON, but it has some extra features.
                          You can find more information about the format on the sponge wiki:
                          https://docs.spongepowered.org/stable/en/server/getting-started/configuration/hocon.html
                          ----------------------------------------------------------------------------------------
                          LibreLogin Configuration
                          ----------------------------------------------------------------------------------------
                          This is the configuration file for LibreLogin.
                          You can find more information about LibreLogin on the github page:
                          https://github.com/kyngs/LibreLogin
                        """,
                logger, new FirstConfigurationMigrator(), new SecondConfigurationMigrator(), new ThirdConfigurationMigrator(), new FourthConfigurationMigrator()
        );

        var helperAdept = adept.getHelper();

        if (!adept.isNewlyCreated() && plugin.getCryptoProvider(helperAdept.get(DEFAULT_CRYPTO_PROVIDER)) == null) {
            throw new CorruptedConfigurationException("Crypto provider not found");
        }

        var timeoutSecs = helperAdept.get(SESSION_TIMEOUT);

        sessionTimeout = timeoutSecs <= 0 ? null : Duration.ofSeconds(timeoutSecs);

        helper = helperAdept;

        return adept.isNewlyCreated();
    }

    public NewUUIDCreator getNewUUIDCreator() {
        var name = get(NEW_UUID_CREATOR);

        try {
            return NewUUIDCreator.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NewUUIDCreator.RANDOM;
        }
    }

    public <T> T get(ConfigurationKey<T> key) {
        return helper.get(key);
    }
}
