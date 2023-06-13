/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.kyngs.librelogin.common.util.CancellableTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PaperUtil {

    private static boolean folia;
    private static Object asyncScheduler;
    private static MethodAccessor entityExecute;
    private static MethodAccessor entityScheduler;
    private static MethodAccessor asyncRunTaskLater;
    private static MethodAccessor asyncRunTaskTimer;
    private static MethodAccessor scheduledCancel;

    static {
        folia = isFolia();
        if (folia) {
            try {
                var getAsyncScheduler = Accessors.getMethodAccessor(Bukkit.getServer().getClass(), "getAsyncScheduler");
                asyncScheduler = getAsyncScheduler.invoke(Bukkit.getServer());
                asyncRunTaskLater = Accessors.getMethodAccessor(asyncScheduler.getClass(), "runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
                asyncRunTaskTimer = Accessors.getMethodAccessor(asyncScheduler.getClass(), "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);

                var scheduledClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                scheduledCancel = Accessors.getMethodAccessor(scheduledClass, "cancel");

                var entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                entityExecute = Accessors.getMethodAccessor(entitySchedulerClass, "execute", Plugin.class, Runnable.class, Runnable.class, long.class);
                entityScheduler = Accessors.getMethodAccessor(Entity.class, "getScheduler");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static CancellableTask runTaskLaterAsynchronously(PaperLibreLogin plugin, Runnable runnable, long delay) {
        if (folia) {
            var task = asyncRunTaskLater.invoke(asyncScheduler, plugin.getBootstrap(), (Consumer<Object>) (o) -> runnable.run(), delay, TimeUnit.MILLISECONDS);
            return () -> scheduledCancel.invoke(task);
        } else {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(), runnable, delay)::cancel;
        }
    }

    public static CancellableTask runTaskTimerAsynchronously(PaperLibreLogin plugin, Runnable runnable, long delay, long period) {
        if (folia) {
            var task = asyncRunTaskTimer.invoke(asyncScheduler, plugin.getBootstrap(), (Consumer<Object>) (o) -> runnable.run(), delay, period, TimeUnit.MILLISECONDS);
            return () -> scheduledCancel.invoke(task);
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin.getBootstrap(), runnable, delay, period)::cancel;
        }
    }

    public static void runSyncAndWait(Runnable runnable, PaperLibreLogin plugin, Player player) {
        if (folia) {
            var scheduler = entityScheduler.invoke(player);
            var future = new CompletableFuture<Void>();
            entityExecute.invoke(scheduler, plugin.getBootstrap(), (Runnable) () -> {
                runnable.run();
                future.complete(null);
            }, null, 0);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (Bukkit.isPrimaryThread()) {
                runnable.run();
            } else {
                var future = new CompletableFuture<Void>();
                Bukkit.getScheduler().runTask(plugin.getBootstrap(), () -> {
                    runnable.run();
                    future.complete(null);
                });
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
