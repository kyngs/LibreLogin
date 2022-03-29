package xyz.kyngs.librepremium.common.util;

public interface ThrowableRunnable<E extends Throwable> {
    void run() throws E;
}
