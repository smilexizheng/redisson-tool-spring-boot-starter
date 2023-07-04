package org.smilexizheng.lock;

import org.smilexizheng.function.SupplierThrowable;
import org.smilexizheng.lock.enums.LockType;

import java.util.concurrent.TimeUnit;

/**
 *
 * 分布式锁 接口
 * @author smile
 */
public interface LockClient {

    boolean tryLock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    void unLock(String lockName, LockType lockType);

    <T> T lock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit, SupplierThrowable<T> supplier);

    default <T> T lockFair(String lockName, long waitTime, long leaseTime, SupplierThrowable<T> supplier) {
        return this.lock(lockName, LockType.FAIR, waitTime, leaseTime, TimeUnit.SECONDS, supplier);
    }

    default <T> T lockReentrant(String lockName, long waitTime, long leaseTime, SupplierThrowable<T> supplier) {
        return this.lock(lockName, LockType.REENTRANT, waitTime, leaseTime, TimeUnit.SECONDS, supplier);
    }
}
