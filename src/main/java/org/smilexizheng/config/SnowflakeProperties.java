package org.smilexizheng.config;


/**
 * 雪花ID生成策略
 * @author smile
 */
public class SnowflakeProperties {

    /**
     * 启用功能
     */
    private Boolean enabled;

    SnowflakeProperties(){
        this.enabled=true;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
