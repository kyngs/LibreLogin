/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import xyz.kyngs.librelogin.api.BiHolder;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;
import xyz.kyngs.librelogin.common.config.migrate.ConfigurationMigrator;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class ConfigurateConfiguration {

    private final ConfigurateHelper helper;
    private final boolean newlyCreated;
    private final HoconConfigurationLoader loader;

    public ConfigurateConfiguration(File dataFolder, String name, Collection<BiHolder<Class<?>, String>> defaultKeys, String comment, Logger logger, ConfigurationMigrator... migrators) throws IOException, CorruptedConfigurationException {
        var revision = migrators.length;
        var file = new File(dataFolder, name);

        if (!file.exists()) {
            newlyCreated = true;
            if (!file.createNewFile()) throw new IOException("Could not create configuration file!");
        } else newlyCreated = false;

        var refHelper = new ConfigurateHelper(CommentedConfigurationNode.root()
                .comment(comment)
        );

        var extractedKeys = defaultKeys.stream()
                .map(data -> new BiHolder<>(GeneralUtil.extractKeys(data.key()), data.value()))
                .toList();

        for (var key : extractedKeys) {
            for (ConfigurationKey<?> configurationKey : key.key()) {
                refHelper.setDefault(configurationKey, key.value());
            }
        }

        var ref = refHelper.configuration();

        var builder = HoconConfigurationLoader.builder()
                .defaultOptions(
                        ConfigurationOptions
                                .defaults()
                                .header(ref.comment())
                )
                .file(file)
                .emitComments(true)
                .prettyPrinting(true);


        loader = builder.build();

        try {
            helper = new ConfigurateHelper(loader.load()
                    .mergeFrom(ref));
        } catch (ConfigurateException e) {
            throw new CorruptedConfigurationException(e);
        }

        var presentRevision = helper.getInt("revision");

        if (presentRevision == null) presentRevision = newlyCreated ? revision : 0;

        if (presentRevision < revision) {
            for (int i = presentRevision; i < revision; i++) {
                migrators[i].migrate(helper, logger);
            }
        }

        helper.configuration().mergeFrom(ref);

        helper.configuration().node("revision")
                .set(revision)
                .comment("The config revision number. !!DO NOT TOUCH THIS!!");

        for (var key : extractedKeys) {
            for (ConfigurationKey<?> configurationKey : key.key()) {
                helper.setComment(configurationKey, key.value());
            }
        }

        save();
    }

    public ConfigurateHelper getHelper() {
        return helper;
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }

    public void save() throws IOException {
        loader.save(helper.configuration());
    }


}
