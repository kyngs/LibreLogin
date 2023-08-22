/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api;

/**
 * A simple holder for two values. Similar to {@link java.util.Map.Entry}, but with a more meaningful name.
 *
 * @param key   The key
 * @param value The value
 * @param <K>   The type of the key
 * @param <V>   The type of the value
 */
public record BiHolder<K, V>(K key, V value) {
}
