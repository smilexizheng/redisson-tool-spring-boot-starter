package org.smilexizheng.lock;

import org.smilexizheng.lock.enums.LockType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * lock注解
 * @author BJWK
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RedissonLock {

    /**
     * 分布式锁key 保证唯一性
     * 可为空
     * @return
     */
    String value() default "";

    /**
     * key 二级参数，支持spel
     * @return
     */
    String param() default "";

    /**
     * 等待超时时间，默认30
     * @return
     */
    long waitTime() default 30L;

    /**
     * 自动解锁时间，必须大于方法执行时间，默认60
     * @return
     */
    long leaseTime() default 60L;

    /**
     * 时间单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * redisson 锁的类型
     * @return
     */
    LockType type() default LockType.FAIR;
}
