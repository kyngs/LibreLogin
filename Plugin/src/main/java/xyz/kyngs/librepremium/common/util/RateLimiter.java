package xyz.kyngs.librepremium.common.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic rate limiter using Caffeine's cache as a backend
 *
 * @param <T> key type
 * @author kyngs
 */
public class RateLimiter<T> {

    private final Cache<T, Object> expiring;

    public RateLimiter(long amount, TimeUnit unit) {
        expiring = Caffeine.newBuilder()
                .ticker(Ticker.systemTicker())
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
