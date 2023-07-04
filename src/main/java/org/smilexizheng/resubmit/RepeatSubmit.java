package org.smilexizheng.resubmit;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防重提交 注解
 * @author smile
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RepeatSubmit {


    /**
     * key 可为空，已保证唯一性
     *
     */
    String value()   default "";

    /**
     * key过期时间
     *
     */
    long expireTime()   default 10L;

    /**
     * key 二级参数，支持spel
     * 可为空
     *
     */
    String param() default "";

    /**
     * 时间单位
     *
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 过期方式
     *
     * true 默认 等待时间自动过期
     * false 方法执行完毕后后过期
     */
    boolean waitExpire() default true;

    /**
     * 是否 校验request表单内容
     */
    boolean validateForm() default true;

}
