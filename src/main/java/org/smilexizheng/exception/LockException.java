package org.smilexizheng.exception;

/**
 * 加锁获取锁失败
 * @author smile
 */
public class LockException extends RuntimeException {
    public LockException(String message) {
        super(message);
    }
}
