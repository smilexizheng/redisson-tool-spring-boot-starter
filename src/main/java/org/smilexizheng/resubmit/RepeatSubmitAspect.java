package org.smilexizheng.resubmit;

import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.smilexizheng.exception.RepeatException;
import org.smilexizheng.exception.SupplierException;
import org.smilexizheng.spel.ExpressionEvaluator;
import org.smilexizheng.utils.CommonUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * 分布式防重复提交
 *
 * @author BJWK
 */
@Aspect
@Order(1)
public class RepeatSubmitAspect {

    private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();

    private ApplicationContext applicationContext;

    private final RedissonClient redissonClient;


    private static final String PREFIX = "repeat-submit:";

    public RepeatSubmitAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(repeatSubmit)")
    public Object aroundRedisLock(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) {
        //签名参数 最终转hex唯一标识
        StringBuffer signature = new StringBuffer();
        signature.append(CommonUtil.getPointSource2Hex(point));


        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String[]> parameterMap = request.getParameterMap();

        //请求的表单参数 循环生成hex 作为签名参数
        parameterMap.values().forEach(i -> {
            signature.append(DigestUtils.md5DigestAsHex(String.join(",", i).getBytes()));
        });
        // 根据签名生成hex 作为key
        String redisKey = DigestUtils.md5DigestAsHex(signature.toString().getBytes());


        String elParam = repeatSubmit.param();
        if (StringUtil.isNotBlank(elParam)) {
            elParam = EVALUATOR.evalPointParam(point, elParam, applicationContext);
            redisKey =  elParam+':'+redisKey ;
        }

        RBucket<String> rBucket = redissonClient.getBucket(PREFIX + redisKey);
        if (rBucket.isExists()) {
            throw new RepeatException("不允许重复提交，请稍后再试");
        }
        long waitTime = repeatSubmit.value() > 1L ? repeatSubmit.value() : 1L;
        rBucket.set("1", waitTime, repeatSubmit.timeUnit());

        try {
            return point.proceed();
        } catch (Throwable throwable) {
            throw new SupplierException("方法执行出错");
        }finally {
            if(!repeatSubmit.waitExpire()){
                rBucket.deleteAsync();
            }
        }
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
