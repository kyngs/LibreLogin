package xyz.kyngs.librelogin.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.EasyDBConfig;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.easydb.provider.mysql.MySQLConfig;
import xyz.kyngs.librelogin.api.Logger;
import xyz.kyngs.librelogin.api.configuration.PluginConfiguration;
import xyz.kyngs.librelogin.api.database.ReadDatabaseProvider;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.migrate.AegisReadProvider;
import xyz.kyngs.librelogin.common.migrate.AuthMeReadProvider;
import xyz.kyngs.librelogin.common.migrate.DBAReadProvider;
import xyz.kyngs.librelogin.common.migrate.JPremiumReadProvider;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;

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
            //noinspection UnstableApiUsage
            component = (TextComponent) component.replaceText(entry.getKey(), Component.text(entry.getValue()));
        }
        return component;
    }

    public static UUID getCrackedUUIDFromName(String name) {
        if (name == null) return null;
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static void checkAndMigrate(PluginConfiguration configuration, Logger logger, AuthenticLibreLogin<?, ?> plugin) {
        if (configuration.migrationOnNextStartup()) {
            logger.info("Performing migration...");

            try {
                logger.info("Connecting to the OLD database...");

                EasyDB<MySQL, Connection, SQLException> easyDB;

                try {
                    easyDB = new EasyDB<>(
                            new EasyDBConfig<>(
                                    new MySQL(
                                            new MySQLConfig()
                                                    .setUsername(configuration.getMigrationOldDatabaseUser())
                                                    .setPassword(configuration.getMigrationOldDatabasePassword())
                                                    .setJdbcUrl("jdbc:mysql://%s:%s/%s?autoReconnect=true".formatted(configuration.getMigrationOldDatabaseHost(), configuration.getMigrationOldDatabasePort(), configuration.getMigrationOldDatabaseName()))
                                    )
                            )
                                    .useGlobalExecutor(true)
                    );

                    logger.info("Connected to the OLD database");

                } catch (Exception e) {
                    var cause = GeneralUtil.getFurthestCause(e);
                    logger.error("!! THIS IS NOT AN ERROR CAUSED BY LIBRELOGIN !!");
                    logger.error("Failed to connect to the OLD database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
                    logger.error("Aborting migration");

                    return;
                }

                try {
                    var localProviders = new HashMap<String, ReadDatabaseProvider>();

                    localProviders.put("JPremium", new JPremiumReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));
                    localProviders.put("AuthMe", new AuthMeReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));
                    localProviders.put("Aegis", new AegisReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));
                    localProviders.put("DBA-SHA-512", new DBAReadProvider(easyDB, configuration.getMigrationOldDatabaseTable(), logger));

                    var provider = localProviders.get(configuration.getMigrationType());

                    if (provider == null) {
                        logger.error("Unknown migrator %s, aborting migration".formatted(configuration.getMigrationType()));
                        return;
                    }

                    logger.info("Starting data conversion... This may take a while!");

                    plugin.migrate(provider, plugin.getDatabaseProvider());

                    logger.info("Migration complete, cleaning up!");

                } finally {
                    easyDB.stop();
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
            } catch (Exception e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}
