package org.smilexizheng.ratelimiter;


import org.redisson.api.RateIntervalUnit;

import java.lang.annotation.*;

/**
 * 限流器 注解
 *
 * @author smile
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RateLimiter {
    /**
     * key 可为空，已保证唯一性
     *
     */
    String value() default "";

    /**
     * key 二级参数，支持spel
     *
     */
    String param() default "";

    /**
     * 速率 默认100
     *
     */
    long rate() default 100L;

    /**
     * 时间 默认1
     *
     */
    long rateInterval() default 1L;

    /**
     * 时间单位 默认分钟
     *
     */
    RateIntervalUnit timeUnit() default RateIntervalUnit.MINUTES;


}
