package com.styzf.link.parser.generator;

import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.OldDataContext;

/**
 * 过滤器
 * @author styzf
 * @date 2023/9/19 23:02
 */
public class FilterUtil {
    
    /**
     * 正则过滤方法，被过滤掉的数据不再解析
     * @param methodName 要解析的方法名
     * @return {@code true}需要解析，{@code false}过滤掉，不解析
     */
    public static boolean filter(String methodName) {
        String filterRegEx = OldDataContext.javaCGConfInfo.getFilterRegEx();
        if (! StrUtil.hasBlank(methodName, filterRegEx)
                && methodName.matches(filterRegEx)) {
            return false;
        }
        return StrUtil.isBlank(methodName)
                || !methodName.contains(":<init>(");
    }
    
    /**
     * 正则过滤方法，被过滤掉的数据不再进行下一层级解析
     * @param methodName 要解析的方法名
     * @return {@code true}需要解析，{@code false}过滤掉，不解析
     */
    public static boolean filterNext(String methodName) {
        String filterNextRegEx = OldDataContext.javaCGConfInfo.getFilterNextRegEx();
        // java原生的方法不进行下一层解析，理论上来说也不存在这种情况
        return StrUtil.isBlank(filterNextRegEx) ||
                !methodName.matches(filterNextRegEx);
    }
}
