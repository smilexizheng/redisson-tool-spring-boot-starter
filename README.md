### 一、项目背景

基于redisson封装通用性组件，目前实现接口限流、加锁、防重提交功能。

### 二、快速开始

```xml

<dependency>
    <groupId>io.github.smilexizheng</groupId>
    <artifactId>redisson-tool-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

组件已经发布到maven中央仓库，[最新版本的依赖](https://central.sonatype.com/artifact/io.github.smilexizheng/redisson-tool-spring-boot-starter)

#### 2.1 项目配置

```yaml
#redisson工具
redisson-tool:
  #启用功能
  enabled: true
  #redisson配置文件
  path: lock/redisson-lock.yml
  #分布式锁
  lock-enabled: true
  #限流
  rate-limiter-enabled: true
  #防重提交
  repeat-submit-enabled: true
```

lock/redisson-lock.yml [参考redisson的声明式配置 ](https://github.com/redisson/redisson/wiki/2.-Configuration#22-declarative-configuration)

```yaml
---
singleServerConfig:
  idleConnectionTimeout: 10000
  connectTimeout: 10000
  timeout: 3000
  retryAttempts: 3
  retryInterval: 1500
  password: 123456789
  subscriptionsPerConnection: 5
  clientName: mainRedis
  address: "redis://127.0.0.1:6379"
  subscriptionConnectionMinimumIdleSize: 1
  subscriptionConnectionPoolSize: 50
  connectionMinimumIdleSize: 24
  connectionPoolSize: 64
  database: 6
  dnsMonitoringInterval: 5000
threads: 16
nettyThreads: 32
codec: !<org.redisson.codec.Kryo5Codec> { }
transportMode: "NIO"
```

#### 2.2 分布式锁

```java
//最小化使用
@RedissonLock
public String get(String userId){
        return "helloWorld"+userId;
}

//完整配置
@RedissonLock(value = "lockKey", param = "#userId", waitTime = 10, leaseTime = 30, type = LockType.FAIR, timeUnit = TimeUnit.SECONDS)
public String get(String userId){
        return "helloWorld"+userId;
}


//编程式使用
@Autowired 
private LockClient lockClient;

public String get(String userId) {  
    String key = "user:"+userId;
    String data= lockClient.lock(key, LockType.FAIR,10,20, TimeUnit.SECONDS,()->{               
          return "hello"+key;
        });
    return data;
}

```
* value：key 默认空，可不填
* param: key的二级参数 支持spel,默认空
* waitTime: 等待时间，默认 30
* leaseTime: 自定解锁时间 默认60
* timeUnit：时间单位 默认秒
* type: 公平锁/普通锁 默认公平锁


#### 2.3 限流器

```java
//最小化使用
@RateLimiter
public String get(String userId){
        return "helloWorld"+userId;
}

//完整配置
@RateLimiter(value="getUserInfo",param = "#userId",rate = 100,rateInterval = 1 ,timeUnit = TimeUnit.MINUTES)
public String get(String userId){
        return "helloWorld"+userId;
}
```
* value：key 默认空，可不填
* param: key的二级参数 支持spel,默认空
* rate: 等待时间，默认 100
* rateInterval: 自定解锁时间 默认 1
* timeUnit：时间单位 默认 分钟

#### 2.4 防重提交

```java
//最小化使用
@RepeatSubmit
public String get(String userId){
        return "helloWorld"+userId;
}

//完整配置
@RepeatSubmit(value="getUserInfo",param = "#userId",expireTime = 10,waitExpire = true,validateForm=true,timeUnit = TimeUnit.SECONDS)
public String get(String userId){
        return "helloWorld"+userId;
}
```
* value：key 默认空，可不填
* param: key的二级参数 支持spel,默认空
* expireTime: 等待时间，默认 10
* waitExpire: true 等待时间自动过期， false 执行完立即过期。默认true
* timeUnit：时间单位 默认 分钟
* validateForm: 是否校验表单 默认true

###  三、 配置说明
#### 3.1 key的自动生成
- value 作为key使用，默认空可不填，使用切入方法的JoinPoint.StaticPart转hex 作为key
- param 作为粒度控制，比如某用户，某类型，支持spel
#### 3.2 混合使用的处理次序
```java
    
    @RedissonLock
    @RepeatSubmit
    @RateLimiter
    public String get(String key) {
        return "hello";
    }
```
- 使用@Order控制切面执行顺序
- 1.@RepeatSubmit     防重提交  
- 2.@RateLimiter      限流器 
- 3.@RedissonLock     分布式锁

### 四、捕获异常 响应式处理
创建异常处理类，GlobalExceptionHandler
```java
/**
 * 自定义异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 限流异常响应
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = RateLimiterException.class)
    @ResponseBody
    public R rateLimiterExceptionHandler(HttpServletRequest req, RateLimiterException e) {
        logger.error(req.getRequestURL().toString());
        logger.error(String.format("已限流：%s，速率：%d/%d", e.getKey(), e.getRate(), e.getTimeUnit().toMillis(e.getRateInterval()) / 1000));
        // 根据自身项目做响应处理      
        return R.error("服务器繁忙");
    }

    /**
     * 加锁异常响应
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = LockException.class)
    @ResponseBody
    public R lockExceptionHandler(HttpServletRequest req, LockException e){
        logger.error(req.getRequestURL().toString());
        logger.error(e.getMessage());
        return R.error("加锁异常");
    }
    .......
}
```
org.smilexizheng.exception 异常类 
- RateLimiterException.class     限流异常
- LockException.class            分布式锁异常
- RepeatException.class          防重提交异常
- SupplierException.class        aop方法执行异常
- SpelEvaluationException.class  spel解析异常

### 最终 欢迎提出使用中的问题






