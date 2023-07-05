package org.smilexizheng.lock;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.smilexizheng.exception.ExceptionType;
import org.smilexizheng.exception.RedissonToolException;
import org.smilexizheng.function.SupplierThrowable;
import org.smilexizheng.lock.enums.LockType;

import java.util.concurrent.TimeUnit;

/**
 *
 * 分布式锁 实现类
 * @author smile
 */
public class LockClientImpl implements LockClient {
    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = this.getLock(lockName, lockType);
        return lock.tryLock(waitTime, leaseTime, timeUnit);
    }

    @Override
    public void unLock(String lockName, LockType lockType) {
        RLock lock = this.getLock(lockName, lockType);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }

    }

    private RLock getLock(String lockName, LockType lockType) {
        RLock lock;
        if (LockType.REENTRANT == lockType) {
            lock = this.redissonClient.getLock(lockName);
        } else {
            lock = this.redissonClient.getFairLock(lockName);
        }

        return lock;
    }

    @Override
    public <T> T lock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit, SupplierThrowable<T> supplier) {
        T obj;
        boolean result;
        try {
            result = this.tryLock(lockName, lockType, waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RedissonToolException(ExceptionType.LockException,"Try Lock has exception,look error log");
        }
        if (!result) {
            throw new RedissonToolException(ExceptionType.TryLockFail,"Try Lock Fail");
        }
        try {
            obj = supplier.get();
        }  catch (Throwable e) {
            e.printStackTrace();
            throw new RedissonToolException(ExceptionType.SupplierException,"Supplier method exception");
        }  finally {
            this.unLock(lockName, lockType);
        }
        return obj;
    }

    public LockClientImpl(final RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
}
