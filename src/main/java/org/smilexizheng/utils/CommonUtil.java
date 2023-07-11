package org.smilexizheng.utils;


import org.springframework.util.DigestUtils;

/**
 * 工具方法
 *
 * @author smile
 */
public class CommonUtil {


    /**
     * MD5哈希值
     *
     * @param str 待处理字符
     * @return a hexadecimal digest string
     */
    public static String getMd5DigestAsHex(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }


}
