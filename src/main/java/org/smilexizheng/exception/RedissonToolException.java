package org.smilexizheng.exception;


/**
 * 工具的异常反馈
 * @author smile
 */
public class RedissonToolException extends RuntimeException {
    private final ExceptionType type;

    public RedissonToolException(ExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    public ExceptionType getType() {
        return this.type;
    }


}