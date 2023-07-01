package org.smilexizheng.utils;


import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.DigestUtils;

/**
 * 工具方法
 * @author smile
 */
public class CommonUtil {


    /**
     * 生成注解的唯一key
     * 切入方法 SourceLocation 转hex
     * @param point
     * @return
     */
    public static String getPointSource2Hex(ProceedingJoinPoint point){
        return DigestUtils.md5DigestAsHex(point.getStaticPart().toLongString().getBytes());
    }


}
