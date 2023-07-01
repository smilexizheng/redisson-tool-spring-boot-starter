package org.smilexizheng.ratelimiter;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * redisson 实现限流器
 * @author smile
 * @Doc https://github.com/redisson/redisson/wiki/6.-%E5%88%86%E5%B8%83%E5%BC%8F%E5%AF%B9%E8%B1%A1#612-%E9%99%90%E6%B5%81%E5%99%A8ratelimiter
 *
 */
public class RateLimiterClientImpl implements RateLimiterClient {
    private final RedissonClient redissonClient;

    public RateLimiterClientImpl(final RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean isAllowed(String key, long rate, long rateInterval, RateIntervalUnit timeUnit) {
        //声明一个限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //设置速率
        rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, timeUnit);
        //设置过期时间
        rateLimiter.expireAsync(Duration.ofMillis(timeUnit.toMillis(rateInterval)));
        //试图获取一个令牌，成功返回true
        return rateLimiter.tryAcquire();

    }
}
