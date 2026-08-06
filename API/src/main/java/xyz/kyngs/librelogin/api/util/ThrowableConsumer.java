/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.util;

/**
 * A functional interface for consuming a value of type T and potentially throwing an exception of type E.
 *
 * @param <T> the type of the value to be consumed
 * @param <E> the type of the exception that can be thrown
 * @see java.util.function.Consumer
 */
public interface ThrowableConsumer<T, E extends Throwable> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws E an exception of type {@link E}
     */
    void accept(T t) throws E;

}
