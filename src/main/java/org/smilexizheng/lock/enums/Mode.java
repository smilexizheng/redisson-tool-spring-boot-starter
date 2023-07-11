package org.smilexizheng.lock.enums;

/**
 * Redisson 连接方式
 * https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter
 * @deprecated
 */
public enum Mode {
    single,
    master,
    sentinel,
    replicated,
    cluster,
    multiCluster;

    private Mode() {
    }
}
