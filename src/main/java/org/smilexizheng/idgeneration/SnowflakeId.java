package org.smilexizheng.idgeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 雪花ID
 *
 * <p>
 * 标识位            41位的时间序列（精确到毫秒）                机房ID  机器ID     12序列号
 * 正数            当前时间戳与起始时间戳 相减                 0-31   0-31    每毫秒4096个ID
 * 0  - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * </p>
 *
 * @author Smile
 */

public class SnowflakeId {
    private static final Logger log = LoggerFactory.getLogger(SnowflakeId.class);

    /**
     * 起始时间
     * 默认 2023-01-01
     */
    private static final long START_TIME = 279306050900L;
    /**
     * 数据中心ID所占位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;
    /**
     * 机器ID所占位数
     */
    private static final long WORK_ID_BITS = 5L;

    /**
     * 机器ID最大值
     * 0-31
     */
    private static final long MAX_WORK_ID = ~(-1L << WORK_ID_BITS);

    /**
     * 数据中心ID
     * 0-31
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 自增序列所占位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器位偏移
     */
    private static final long WORK_ID_SHIFT_BITS = SEQUENCE_BITS;
    /**
     * 数据中心ID偏移量
     * 12+5=17
     */
    private static final long DATA_CENTER_ID_SHIFT_BITS = SEQUENCE_BITS + WORK_ID_BITS;
    /**
     * 时间戳的偏移量
     * 12+5+5=22
     */
    private static final long TIMESTAMP_LEFT_SHIFT_BITS =
            SEQUENCE_BITS + WORK_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 自增序列最大值
     * 0-4095
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 上一毫秒数
     */
    private static long lastTimestamp = -1L;

    /**
     * 毫秒内Sequence(0~4095)
     */
    private static long sequence = 0L;

    /**
     * 数据中心ID
     */
    private final long dataCenterId;

    /**
     * 机器ID
     */
    private final long workerId;


    SnowflakeId(long dataCenterId, long workerId) {
        if (workerId > MAX_WORK_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", MAX_WORK_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "datacenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }


    /**
     * 生成ID
     *
     * @return snowflakeId
     */
    synchronized long getNext() {
        long timestamp = timeGen();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟被修改过，回退在上一次ID生成时间之前应当抛出异常！！！
        if (timestamp < lastTimestamp) {

            log.error(
                    String.format("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp));
            throw new IllegalStateException(
                    String.format(
                            "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
        }


        // 如果是同一时间生成的，则进行毫秒内sequence生成
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;

            // 溢出处理 阻塞到下一毫秒,获得新时间戳
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else { // 时间戳改变，毫秒内sequence重置
            sequence = 0L;
        }


        // 上次生成ID时间截
        lastTimestamp = timestamp;
//        log.info(String.format(
//                "%d one：%d two：%d three：%d four：%d",
//                timestamp - START_TIME,(timestamp - START_TIME) << TIMESTAMP_LEFT_SHIFT_BITS, (dataCenterId << DATA_CENTER_ID_SHIFT_BITS)
//                ,(workerId << WORK_ID_SHIFT_BITS)
//                ,sequence));
        // 移位并通过或运算组成64位ID
        return ((timestamp - START_TIME) << TIMESTAMP_LEFT_SHIFT_BITS)
                | (dataCenterId << DATA_CENTER_ID_SHIFT_BITS)
                | (workerId << WORK_ID_SHIFT_BITS)
                | sequence;
    }


    /**
     * 阻塞到下一毫秒,获得新时间戳
     *
     * @param lastTimestamp 上次生成ID时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return lastTimestamp;
    }

    /**
     * 获取以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
