package org.smilexizheng.exception;

/**
 * 防重提交异常
 * @author smile
 */
public class RepeatException extends RuntimeException {
    public RepeatException(String message) {
        super(message);
    }
}
