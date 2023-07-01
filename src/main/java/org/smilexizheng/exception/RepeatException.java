package org.smilexizheng.exception;

/**
 * 防重提交异常
 * @author BJWK
 */
public class RepeatException extends RuntimeException {
    public RepeatException(String message) {
        super(message);
    }
}
