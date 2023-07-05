package org.smilexizheng.exception;


/**
 * @author smile
 */

public enum ExceptionType {

    /**
     * 分布式锁异常
     */
    LockException,
    /**
     * 获取锁失败
     */
    TryLockFail,
    /**
     * 防重提交异常
     */
    RepeatException,
    /**
     * 限流异常
     */
    RateLimiterException,
    /**
     * spel 解析异常
     */
    SpElException,
    /**
     * 切面方法执行异常
     */
    SupplierException;

     ExceptionType() {
    }


}
