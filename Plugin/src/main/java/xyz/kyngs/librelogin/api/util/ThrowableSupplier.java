package xyz.kyngs.librelogin.api.util;

public interface ThrowableSupplier<T, E extends Throwable> {
    T get() throws E;
}
