package org.smilexizheng.resubmit;

import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.smilexizheng.exception.ExceptionType;
import org.smilexizheng.exception.RedissonToolException;
import org.smilexizheng.spel.ExpressionEvaluator;
import org.smilexizheng.utils.CommonUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * 防重复提交切面
 *
 * @author smile
 */
@Aspect
@Order(1)
public class RepeatSubmitAspect {

    private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();

    @Autowired
    private ApplicationContext applicationContext;

    private final RedissonClient redissonClient;


    private static final String PREFIX = "repeat-submit";

    public RepeatSubmitAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(repeatSubmit)")
    public Object aroundRedisLock(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) {


        StringBuilder redisKey = new StringBuilder(PREFIX);
        String pointHex = CommonUtil.getMd5DigestAsHex(point.getStaticPart().toLongString());
        String key = repeatSubmit.value();
        if(StringUtil.isBlank(key)){
            key=pointHex;
        }
        redisKey.append(":").append(key);

        String elParam = repeatSubmit.param();
        if (StringUtil.isNotBlank(elParam)) {
            elParam = EVALUATOR.evalPointParam(point, elParam, applicationContext);
            redisKey.append(":").append(elParam);
        }

        if(repeatSubmit.validateForm()){
            //签名参数 最终转hex唯一标识
            StringBuffer signature = new StringBuffer(pointHex);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Map<String, String[]> parameterMap = request.getParameterMap();

            //请求的表单参数 生成hex 作为签名参数
            parameterMap.values().forEach(i ->
                    signature.append(DigestUtils.md5DigestAsHex(String.join(",", i).getBytes()))
            );
            redisKey.append(":").append(DigestUtils.md5DigestAsHex(signature.toString().getBytes()));
        }

        RBucket<String> rBucket = redissonClient.getBucket(redisKey.toString());
        if (rBucket.isExists()) {
            throw new RedissonToolException(ExceptionType.isRepeatSubmit,"Duplicate submissions are not allowed. Please try again later");
        }
        long waitTime = Math.max(repeatSubmit.expireTime(), 1L);
        rBucket.set("1", waitTime, repeatSubmit.timeUnit());

        try {
            return point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RedissonToolException(ExceptionType.SupplierException,"Supplier method exception");
        }finally {
            if(!repeatSubmit.waitExpire()){
                rBucket.deleteAsync();
            }
        }
    }




}
