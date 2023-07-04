package org.smilexizheng.lock;

import org.smilexizheng.lock.enums.LockType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * 分布式锁 注解
 *
 * @author smile
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RedissonLock {

    /**
     * 分布式锁key，可为空，已保证唯一性
     *
     */
    String value() default "";

    /**
     * key 二级参数，支持spel
     */
    String param() default "";

    /**
     * 等待超时时间，默认30
     */
    long waitTime() default 30L;

    /**
     * 自动解锁时间，必须大于方法执行时间，默认60
     */
    long leaseTime() default 60L;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * redisson 锁的类型
     */
    LockType type() default LockType.FAIR;
}
