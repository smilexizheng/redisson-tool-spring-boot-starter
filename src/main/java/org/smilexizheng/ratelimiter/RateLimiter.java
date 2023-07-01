package org.smilexizheng.ratelimiter;


import org.redisson.api.RateIntervalUnit;

import java.lang.annotation.*;

/**
 * 限流器
 *
 * @author BJWK
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RateLimiter {
    /**
     * key，保证唯一性
     * 可为空
     * @return
     */
    String value() default "";

    /**
     * key 二级参数，支持spel
     *
     * @return
     */
    String param() default "";

    /**
     * 速率 默认100
     *
     * @return
     */
    long rate() default 100L;

    /**
     * 时间 默认1
     *
     * @return
     */
    long rateInterval() default 1L;

    /**
     * 时间单位 默认分钟
     *
     * @return
     */
    RateIntervalUnit timeUnit() default RateIntervalUnit.MINUTES;


}