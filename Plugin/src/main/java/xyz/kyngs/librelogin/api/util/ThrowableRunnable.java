package xyz.kyngs.librelogin.api.util;

public interface ThrowableRunnable<E extends Throwable> {
    void run() throws E;
}
