package org.smilexizheng.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smilexizheng.lock.LockAspect;
import org.smilexizheng.lock.LockClient;
import org.smilexizheng.lock.LockClientImpl;
import org.smilexizheng.ratelimiter.RateLimiterAspect;
import org.smilexizheng.ratelimiter.RateLimiterClient;
import org.smilexizheng.ratelimiter.RateLimiterClientImpl;
import org.smilexizheng.resubmit.RepeatSubmitAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

/**
 * @author BJWK
 */
@AutoConfiguration
@EnableConfigurationProperties({RedissonProperties.class})
@ConditionalOnClass({RedissonClient.class})
@ConditionalOnProperty(
        value = {"redisson.enabled"},
        havingValue = "true"
)
public class BeanAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(BeanAutoConfiguration.class);

    private RedissonClient redissonClient;

    public BeanAutoConfiguration(RedissonProperties properties){
        logger.info("Initializing Redisson Utils");
        this.redissonClient = getRedissonClient(properties);
    }

    private RedissonClient getRedissonClient(RedissonProperties properties){
        if(null==redissonClient){
            ResourceLoader loader = new DefaultResourceLoader();
            Resource resource = loader.getResource(properties.getPath());
            if (resource.exists()) {
                Config config = null;
                try {
                    config = Config.fromYAML(resource.getInputStream());
                } catch (IOException e) {
                    logger.error("Redisson Yaml file read failed");
                    e.printStackTrace();
                }
                redissonClient = Redisson.create(config);
                logger.info("Redisson connection completed");
            }
        }
        return redissonClient;
    }


    @Bean
    @Order(1)
    @ConditionalOnProperty(
            value = {"redisson.lock-enabled"},
            havingValue = "true"
    )
    @ConditionalOnMissingBean
    public LockClient redisLockClient(){
        LockClientImpl lockClient = new LockClientImpl(this.redissonClient);
        logger.info("Distributed Locks Successfully");
        return lockClient;
    }


    @Bean
    @Order(2)
    @ConditionalOnProperty(
            value = {"redisson.rate-limiter-enabled"},
            havingValue = "true"
    )
    @ConditionalOnMissingBean
    public RateLimiterClient rateLimiterClient(){
        RateLimiterClientImpl rateLimiterClient = new RateLimiterClientImpl(this.redissonClient);
        logger.info("RateLimiter Successfully");
        return rateLimiterClient;
    }


    @Bean
    @Order(3)
    @ConditionalOnBean(LockClient.class)
    @ConditionalOnMissingBean
    public LockAspect redisLockAspect(LockClient redisLockClient) {
        return new LockAspect(redisLockClient);
    }


    @Bean
    @Order(4)
    @ConditionalOnBean(RateLimiterClient.class)
    @ConditionalOnMissingBean
    public RateLimiterAspect rateLimiterAspect(RateLimiterClient rateLimiterClient) {
        return new RateLimiterAspect(rateLimiterClient);
    }

    @Bean
    @Order(5)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            value = {"redisson.repeat-submit-enabled"},
            havingValue = "true"
    )
    public RepeatSubmitAspect repeatSubmitAspect() {
        logger.info("RepeatSubmit Successfully");
        return new RepeatSubmitAspect(this.redissonClient);
    }


}