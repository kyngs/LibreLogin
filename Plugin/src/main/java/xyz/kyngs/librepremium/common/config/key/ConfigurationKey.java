package xyz.kyngs.librepremium.common.config.key;

import xyz.kyngs.librepremium.common.config.ConfigurateHelper;

import java.util.function.BiFunction;

public record ConfigurationKey<T>(String key, T defaultValue, String comment,
                                  BiFunction<ConfigurateHelper, String, T> getter) {

}
