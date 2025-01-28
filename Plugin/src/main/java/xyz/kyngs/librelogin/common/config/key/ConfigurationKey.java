/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config.key;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.common.config.ConfigurateHelper;

import java.util.function.BiFunction;

public class ConfigurationKey<T> {

    private final String key;
    private final BiFunction<ConfigurateHelper, String, T> getter;
    private final Setter<T> setter;
    private T defaultValue;
    private String comment;

    public ConfigurationKey(String key, T defaultValue, String comment, BiFunction<ConfigurateHelper, String, T> getter) {
        this(key, defaultValue, comment, getter, null);
    }

    public ConfigurationKey(String key, T defaultValue, String comment, BiFunction<ConfigurateHelper, String, T> getter, @Nullable Setter<T> setter) {
        this.key = key;
        this.getter = getter;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.setter = setter == null ? ConfigurateHelper::set : setter;
    }

    public static ConfigurationKey<?> createCommentKey(String key, String comment) {
        return new ConfigurationKey<>(key, null, comment, (x, y) -> {
            throw new UnsupportedOperationException();
        });
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String key() {
        return key;
    }

    public BiFunction<ConfigurateHelper, String, T> getter() {
        return getter;
    }

    public Setter<T> setter() {
        return setter;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public String comment() {
        return comment;
    }

    public void setDefault(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T compute(ConfigurateHelper configurateHelper) {
        var value = getter.apply(configurateHelper, key());

        return value == null ? defaultValue : value;
    }

    public interface Setter<T> {
        void set(ConfigurateHelper helper, String key, T value);
    }
}
