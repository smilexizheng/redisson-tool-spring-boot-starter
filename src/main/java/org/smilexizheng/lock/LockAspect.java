package org.smilexizheng.lock;

import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.smilexizheng.lock.enums.LockType;
import org.smilexizheng.spel.ExpressionEvaluator;
import org.smilexizheng.utils.CommonUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;


/**
 *
 *
 * @author smile
 */
@Aspect
@Order(3)
public class LockAspect implements ApplicationContextAware {

    private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();
    private final LockClient redisLockClient;
    private ApplicationContext applicationContext;


    private static final String PREFIX = "lock:";

    @Around("@annotation(redisLock)")
    public Object aroundRedisLock(ProceedingJoinPoint point, RedissonLock redisLock) {
        String lockKey = redisLock.value();
        if(StringUtil.isBlank(lockKey)){
            lockKey = CommonUtil.getPointSource2Hex(point);
        }
        Assert.hasText(lockKey, "@RedissonLock key must not be null or empty");
        String lockParam = redisLock.param();

        if (StringUtil.isNotBlank(lockParam)) {
                lockKey +=  ':' + EVALUATOR.evalPointParam(point, lockParam,applicationContext);
        }

        LockType lockType = redisLock.type();
        long waitTime = redisLock.waitTime();
        long leaseTime = redisLock.leaseTime();
        TimeUnit timeUnit = redisLock.timeUnit();
        return this.redisLockClient.lock(PREFIX+lockKey, lockType, waitTime, leaseTime, timeUnit, point::proceed);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public LockAspect(final LockClient redisLockClient) {
        this.redisLockClient = redisLockClient;
    }

}
