package org.smilexizheng.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author BJWK
 */
@ConfigurationProperties("redisson")
public class RedissonProperties {

    /**
     * 启用功能
     */
    private Boolean enabled;

    /**
     * 启用分布式锁
     */
    private Boolean lockEnabled;
    /**
     * 启用限流
     */
    private Boolean rateLimiterEnabled;
    /**
     * 启用防重提交
     */
    private Boolean repeatSubmitEnabled;
    /**
     * redisson配置文件的路径
     */
    private String path;

    public RedissonProperties() {
        this.enabled = Boolean.FALSE;
        this.lockEnabled = Boolean.TRUE;
        this.rateLimiterEnabled = Boolean.TRUE;
        this.repeatSubmitEnabled = Boolean.TRUE;
        this.path = "redisson-lock.yml";
    }

    public Boolean getEnabled() {
        return this.enabled;
    }


    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getLockEnabled() {
        return lockEnabled;
    }

    public void setLockEnabled(Boolean lockEnabled) {
        this.lockEnabled = lockEnabled;
    }

    public Boolean getRateLimiterEnabled() {
        return rateLimiterEnabled;
    }

    public void setRateLimiterEnabled(Boolean rateLimiterEnabled) {
        this.rateLimiterEnabled = rateLimiterEnabled;
    }

    public Boolean getRepeatSubmitEnabled() {
        return repeatSubmitEnabled;
    }

    public void setRepeatSubmitEnabled(Boolean repeatSubmitEnabled) {
        this.repeatSubmitEnabled = repeatSubmitEnabled;
    }
}
