package org.smilexizheng.exception;


/**
 * 工具异常类
 *
 * @author smile
 */
public enum ExceptionType {
    /**
     * 获取锁失败
     * <p>
     * 尝试加锁失败
     */
    TryLockFail,
    /**
     * 防重提交异常
     * <p>
     * 已重复提交
     */
    isRepeatSubmit,
    /**
     * 限流异常
     * <p>
     * 已限流
     */
    isRateLimiter,
    /**
     * 分布式锁异常
     * <p>
     * 分布式锁加锁 异常, 建议:查看错误日志信息
     */
    LockException,
    /**
     * spel 解析异常
     * <p>
     * 解析参数异常 建议:查看错误日志信息
     */
    SpElException,
    /**
     * 切面方法执行异常
     * <p>
     * 切面方法执行失败 建议:查看错误日志信息
     */
    SupplierException;

    ExceptionType() {
    }


}
