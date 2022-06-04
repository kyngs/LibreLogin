package xyz.kyngs.librepremium.common.config.key;

import xyz.kyngs.librepremium.common.config.ConfigurateHelper;

import java.util.function.BiFunction;

public record ConfigurationKey<T>(String key, T defaultValue, String comment,
                                  BiFunction<ConfigurateHelper, String, T> getter) {

    public T compute(ConfigurateHelper configurateHelper) {
        var value = getter.apply(configurateHelper, key);

        return value == null ? defaultValue : value;
    }

    public static ConfigurationKey<?> comment(String key, String comment) {
        return new ConfigurationKey<>(key, null, comment, (x, y) -> {
            throw new UnsupportedOperationException();
        });
    }
}
