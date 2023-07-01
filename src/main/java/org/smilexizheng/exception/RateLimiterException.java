package org.smilexizheng.exception;

import org.redisson.api.RateIntervalUnit;

/**
 * @author smile
 */
public class RateLimiterException extends RuntimeException {
    private final String key;
    private final long rate;
    private final long rateInterval;
    private final RateIntervalUnit timeUnit;

    public RateLimiterException(String key, long rate, long rateInterval, RateIntervalUnit timeUnit) {
        super(String.format("访问限流：%s，速率：%d/%d", key, rate, timeUnit.toMillis(rateInterval)/1000));
        this.key = key;
        this.rate = rate;
        this.rateInterval = rateInterval;
        this.timeUnit = timeUnit;
    }

    public String getKey() {
        return this.key;
    }

    public long getRate() {
        return this.rate;
    }

    public long getRateInterval() {
        return this.rateInterval;
    }

    public RateIntervalUnit getTimeUnit() {
        return this.timeUnit;
    }
}

