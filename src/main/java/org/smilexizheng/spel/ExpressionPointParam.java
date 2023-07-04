package org.smilexizheng.spel;

import java.lang.reflect.Method;

/**
 * 解析point参数
 * @author BJWK
 */
public class ExpressionPointParam {

    private final Method method;
    private final Object[] args;
    private final Object target;
    private final Class<?> targetClass;
    private final Method targetMethod;

    public Method getMethod() {
        return this.method;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public Object getTarget() {
        return this.target;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Method getTargetMethod() {
        return this.targetMethod;
    }

    public ExpressionPointParam(final Method method, final Object[] args, final Object target, final Class<?> targetClass, final Method targetMethod) {
        this.method = method;
        this.args = args;
        this.target = target;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }
}
