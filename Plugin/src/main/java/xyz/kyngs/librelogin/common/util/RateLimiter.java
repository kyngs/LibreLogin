/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic rate limiter using Caffeine's cache as a backend.<br>
 * <b>This implementation is thread-safe.</b>
 *
 * @param <T> key type
 * @author kyngs
 */
public class RateLimiter<T> {

    private final Cache<T, Object> expiring;

    public RateLimiter(long amount, TimeUnit unit) {
        expiring = Caffeine.newBuilder()
                .expireAfterWrite(amount, unit)
                .build();
    }

    /**
     * @param t key
     * @return true, if rate limiting occurred, false otherwise
     */
    public boolean tryAndLimit(T t) {
        var wasLimited = new AtomicBoolean(true);
        expiring.get(t, x -> {
            wasLimited.set(false);
            return new Object();
        });
        return wasLimited.get();
    }

}
