/*
 * Copyright (c) 2021 kyngs
 *
 * Please see the included "LICENSE" file for further information about licensing of this code.
 *
 * !!Removing this notice is a direct violation of the license!!
 */

package xyz.kyngs.librelogin.api.util;

public interface ThrowableConsumer<T, E extends Throwable> {

    void accept(T t) throws E;

}
