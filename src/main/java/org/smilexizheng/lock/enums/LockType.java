package org.smilexizheng.lock.enums;


/**
 * redisson 锁的方式
 *
 * @author smile
 */
public enum LockType {
    /**
     * 普通锁
     * 多个线程去获取锁的时候，会直接去尝试获取，获取不到，再去进入等待队列
     */
    REENTRANT,
    /**
     * 公平锁
     * 多个线程按照申请锁的顺序去获得锁，线程会直接进入队列去排队
     */
    FAIR;

    private LockType() {
    }
}
