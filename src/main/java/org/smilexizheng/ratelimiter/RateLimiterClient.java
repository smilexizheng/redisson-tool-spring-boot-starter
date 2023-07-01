package org.smilexizheng.ratelimiter;

import org.redisson.api.RateIntervalUnit;
import org.smilexizheng.exception.RateLimiterException;
import org.smilexizheng.exception.SupplierException;
import org.smilexizheng.function.SupplierThrowable;

/**
 * 限流器
 */
public interface RateLimiterClient {

    /**
     * 是否限流
     * @param key
     * @param rate
     * @param rateInterval
     * @param timeUnit
     * @return
     */
    boolean isAllowed(String key, long rate, long rateInterval, RateIntervalUnit timeUnit);


    /**
     * 是否限流
     * @param key
     * @param rate
     * @param rateInterval
     * @return
     */
    default boolean isAllowed(String key, long rate, long rateInterval) {
        return this.isAllowed(key, rate, rateInterval, RateIntervalUnit.SECONDS);
    }


    /**
     *
     * @param key
     * @param rate
     * @param rateInterval
     * @param supplier
     * @param <T>
     * @return
     */
    default <T> T allow(String key, long rate, long rateInterval, SupplierThrowable<T> supplier) {
        return this.allow(key, rate, rateInterval, RateIntervalUnit.SECONDS, supplier);
    }

    default <T> T allow(String key, long rate, long rateInterval, RateIntervalUnit timeUnit, SupplierThrowable<T> supplier) {
        boolean isAllowed = this.isAllowed(key, rate, rateInterval, timeUnit);
        if (!isAllowed) {
            throw new RateLimiterException(key, rate, rateInterval, timeUnit);
        }
        try {
            return supplier.get();
        }  catch (Throwable e) {
            e.printStackTrace();
            throw new SupplierException(key + ",supplier method throwable");
        }
    }


}
