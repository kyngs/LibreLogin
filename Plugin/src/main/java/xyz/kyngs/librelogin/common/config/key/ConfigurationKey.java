/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.config.key;

import xyz.kyngs.librelogin.common.config.ConfigurateHelper;

import java.util.function.BiFunction;

public class ConfigurationKey<T> {

    private final String key;
    private final BiFunction<ConfigurateHelper, String, T> getter;
    private T defaultValue;
    private String comment;

    public ConfigurationKey(String key, T defaultValue, String comment, BiFunction<ConfigurateHelper, String, T> getter) {
        this.key = key;
        this.getter = getter;
        this.defaultValue = defaultValue;
        this.comment = comment;
    }

    public static ConfigurationKey<?> getComment(String key, String comment) {
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
}
