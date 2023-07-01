package org.smilexizheng.resubmit;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 放重提交 注解
 * @author BJWK
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RepeatSubmit {

    /**
     * 防重等待解锁时间
     * @return
     */
    long value()   default 10L;

    /**
     * key参数，支持spel
     * 可为空
     * @return
     */
    String param() default "";

    /**
     * 时间单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 过期方式
     *
     * true 默认 防重等待时间过期
     * false 方法执行完毕后后过期
     */
    boolean waitExpire() default true;

}
