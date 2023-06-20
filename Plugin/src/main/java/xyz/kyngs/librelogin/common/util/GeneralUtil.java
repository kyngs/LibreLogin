/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.util;

import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.database.ReadDatabaseProvider;
import xyz.kyngs.librelogin.api.database.connector.DatabaseConnector;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.config.HoconPluginConfiguration;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;

import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.DATABASE_TYPE;
import static xyz.kyngs.librelogin.common.config.ConfigurationKeys.MIGRATION_TYPE;

public class GeneralUtil {

    public static final ForkJoinPool ASYNC_POOL = new ForkJoinPool(4);

    public static String readInput(InputStream inputStream) throws IOException {
        var input = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();
        return input;
    }

    public static Throwable getFurthestCause(Throwable throwable) {
        while (true) {
            var cause = throwable.getCause();

            if (cause == null) return throwable;

            throwable = cause;
        }
    }

    public static UUID fromUnDashedUUID(String id) {
        return new UUID(
                new BigInteger(id.substring(0, 16), 16).longValue(),
                new BigInteger(id.substring(16, 32), 16).longValue()
        );
    }

    @Nullable
    public static TextComponent formatComponent(@Nullable TextComponent component, Map<String, String> replacements) {
        if (component == null) return null;

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            component = (TextComponent) component.replaceText(builder -> builder.matchLiteral(entry.getKey()).replacement(entry.getValue()));
        }
        return component;
    }

    public static UUID getCrackedUUIDFromName(String name) {
        if (name == null) return null;
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static void checkAndMigrate(HoconPluginConfiguration configuration, Logger logger, AuthenticLibreLogin<?, ?> plugin) {
        if (configuration.get(ConfigurationKeys.MIGRATION_ON_NEXT_STARTUP)) {
            logger.info("Performing migration...");

            try {
                logger.info("Connecting to the OLD database...");

                ReadDatabaseProvider provider;
                DatabaseConnector<?, ?> connector = null;

                try {
                    var registration = plugin.getReadProviders().get(configuration.get(MIGRATION_TYPE));
                    if (registration == null) {
                        logger.error("Migration type %s doesn't exist, please check your configuration".formatted(configuration.get(MIGRATION_TYPE)));
                        logger.error("Aborting migration");
                        return;
                    }

                    if (registration.databaseConnector() != null) {
                        var connectorRegistration = plugin.getDatabaseConnector(registration.databaseConnector());

                        if (connectorRegistration == null) {
                            logger.error("Migration type %s is corrupted, please use a different one".formatted(configuration.get(DATABASE_TYPE)));
                            logger.error("Aborting migration");
                            return;
                        }

                        connector = connectorRegistration.factory().apply("migration.old-database." + connectorRegistration.id() + ".");

                        connector.connect();
                    }

                    provider = registration.create(connector);

                    logger.info("Connected to the OLD database");

                } catch (Exception e) {
                    var cause = GeneralUtil.getFurthestCause(e);
                    logger.error("!! THIS IS NOT AN ERROR CAUSED BY LIBRELOGIN !!");
                    logger.error("Failed to connect to the OLD database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
                    logger.error("Aborting migration");

                    return;
                }

                try {
                    logger.info("Starting data conversion... This may take a while!");

                    plugin.migrate(provider, plugin.getDatabaseProvider());

                    logger.info("Migration complete, cleaning up!");

                } finally {
                    if (connector != null) connector.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("An unexpected exception occurred while performing database migration, aborting migration");
            }

        }
    }

    public static CompletionStage<Void> runAsync(Runnable runnable) {
        var future = new CompletableFuture<Void>();
        AuthenticLibreLogin.EXECUTOR.submit(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (InvalidCommandArgument e) {
                future.completeExceptionally(e);
            } catch (Throwable e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static List<ConfigurationKey<?>> extractKeys(Class<?> clazz) {
        var list = new ArrayList<ConfigurationKey<?>>();
        try {
            for (Field field : clazz.getFields()) {
                if (field.getType() != ConfigurationKey.class) continue;
                list.add((ConfigurationKey<?>) field.get(null));

            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

}
