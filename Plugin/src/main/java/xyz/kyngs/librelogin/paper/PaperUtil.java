package xyz.kyngs.librelogin.paper;

import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PaperUtil {

    public static void runSyncAndWait(Runnable runnable, PaperLibreLogin plugin) {
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
