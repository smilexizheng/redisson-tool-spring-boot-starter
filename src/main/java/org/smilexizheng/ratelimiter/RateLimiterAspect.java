package org.smilexizheng.ratelimiter;


import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.smilexizheng.spel.ExpressionEvaluator;
import org.smilexizheng.utils.CommonUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

/**
 * 限流器AOP 切面
 *
 * @author smile
 */
@Aspect
@Order(2)
public class RateLimiterAspect implements ApplicationContextAware {

    private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();

    private ApplicationContext applicationContext;

    private final RateLimiterClient rateLimiterClient;

    private static final String PREFIX = "rate-limiter:";


    @Around("@annotation(rateLimiter)")
    public  Object aroundRateLimiter(ProceedingJoinPoint point, RateLimiter rateLimiter){
        String redisKey = rateLimiter.value();
        if(StringUtil.isBlank(redisKey)){
            redisKey = CommonUtil.getMd5DigestAsHex(point.getStaticPart().toLongString());
        }
        Assert.hasText(redisKey, "@RateLimiter key must not be null or empty");
        String lockParam = rateLimiter.param();
        if (StringUtil.isNotBlank(lockParam)) {
            redisKey +=  ':' + EVALUATOR.evalPointParam(point, lockParam,applicationContext);
        }
        return this.rateLimiterClient.allow(PREFIX+redisKey,  rateLimiter.rate(), rateLimiter.rateInterval(), rateLimiter.timeUnit(), point::proceed);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    public RateLimiterAspect(RateLimiterClient rateLimiterClient){
        this.rateLimiterClient =rateLimiterClient;
    }
}
