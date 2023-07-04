package org.smilexizheng.spel;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;

/**
 * spel 解析器
 * @author smile
 */
public class ExpressionEvaluator extends CachedExpressionEvaluator {
    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap(64);
    private final Map<AnnotatedElementKey, Method> methodCache = new ConcurrentHashMap(64);

    public ExpressionEvaluator() {
    }

    public EvaluationContext createContext(Method method, Object[] args, Object target, Class<?> targetClass, @Nullable BeanFactory beanFactory) {
        Method targetMethod = this.getTargetMethod(targetClass, method);
        ExpressionPointParam rootObject = new ExpressionPointParam(method, args, target, targetClass, targetMethod);
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, targetMethod, args, this.getParameterNameDiscoverer());
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }

        return evaluationContext;
    }

    public EvaluationContext createContext(Method method, Object[] args, Class<?> targetClass, Object rootObject, @Nullable BeanFactory beanFactory) {
        Method targetMethod = this.getTargetMethod(targetClass, method);
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, targetMethod, args, this.getParameterNameDiscoverer());
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }

        return evaluationContext;
    }

    @Nullable
    public Object eval(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return this.eval(expression, methodKey, evalContext, (Class)null);
    }

    @Nullable
    public <T> T eval(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext, @Nullable Class<T> valueType) {
        return this.getExpression(this.expressionCache, methodKey, expression).getValue(evalContext, valueType);
    }

    @Nullable
    public String evalAsText(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return this.eval(expression, methodKey, evalContext, String.class);
    }

    @Nullable
    public String evalPointParam(ProceedingJoinPoint point, String lockParam, ApplicationContext applicationContext) {
        MethodSignature ms = (MethodSignature)point.getSignature();
        Method method = ms.getMethod();
        Object[] args = point.getArgs();
        Object target = point.getTarget();
        Class<?> targetClass = target.getClass();
        EvaluationContext context = this.createContext(method, args, target, targetClass, applicationContext);
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, targetClass);
        return this.evalAsText(lockParam, elementKey, context);
    }

    public boolean evalAsBool(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return Boolean.TRUE.equals(this.eval(expression, methodKey, evalContext, Boolean.class));
    }

    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        return this.methodCache.computeIfAbsent(methodKey, (key) -> AopUtils.getMostSpecificMethod(method, targetClass));
    }

    public void clear() {
        this.expressionCache.clear();
        this.methodCache.clear();
    }
}

