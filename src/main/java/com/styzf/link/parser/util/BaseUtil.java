package com.styzf.link.parser.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * 基础工具类
 * @author styzf
 * @date 2023/9/15 22:57
 */
public class BaseUtil {
    
    public static String getEasyMethodName(String methodName) {
        Pattern pattern = compile("(?<=:).*?(?=\\()");
        Matcher matcher = pattern.matcher(methodName);
        String easyMethodName = "";
        if (matcher.find()) {
            easyMethodName = matcher.group();
        }
        
        pattern = compile("(?<=\\.)((.(?!(\\.|\\()))*.)(?=(,|\\)))");
        matcher = pattern.matcher(easyMethodName);
        StringBuilder args = new StringBuilder();
        while (matcher.find()) {
            args.append("_").append(matcher.group());
        }
        
        return getEasyClassName(methodName) + "_" + easyMethodName + args;
    }
    
    public static String getEasyClassName(String methodName) {
        Pattern pattern = compile("(?<=\\.)((.(?!\\.))*.)(?=:)");
        Matcher matcher = pattern.matcher(methodName);
        String className = "";
        if (matcher.find()) {
            className = matcher.group();
        }
        
        return className;
    }
    
    public static String getClassName(String methodName) {
        Pattern pattern = compile("(?<=\\.)((.(?!\\.))*.)(?=:)");
        Matcher matcher = pattern.matcher(methodName);
        String className = "";
        if (matcher.find()) {
            className = matcher.group();
        }
        if (! methodName.contains(":")) {
            return methodName;
        }
        
        return methodName.substring(0, methodName.indexOf(":"));
    }
}
