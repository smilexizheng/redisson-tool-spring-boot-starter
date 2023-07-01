package org.smilexizheng.lock;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.smilexizheng.exception.LockException;
import org.smilexizheng.exception.SupplierException;
import org.smilexizheng.function.SupplierThrowable;
import org.smilexizheng.lock.enums.LockType;

import java.util.concurrent.TimeUnit;

/**
 * @author BJWK
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
        T obj = null;
        boolean result = false;
        try {
            result = this.tryLock(lockName, lockType, waitTime, leaseTime, timeUnit);
            if (!result) {
                throw new LockException(lockName+",tryLock false");
            }
            obj = supplier.get();
        } catch (LockException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new SupplierException(lockName + ",supplier method throwable");
        } finally {
            this.unLock(lockName, lockType);
        }
        return obj;
    }

    public LockClientImpl(final RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
}
