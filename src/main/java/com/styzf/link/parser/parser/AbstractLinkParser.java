package com.styzf.link.parser.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.call.MethodCall;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基础链路解析代码
 * @author styzf
 * @date 2023/9/7 22:17
 */
public abstract class AbstractLinkParser implements ParserInterface {
    
    /**
     * 用于去重的set
     */
    protected static final Set<String> METHOD_SET = new HashSet<>();
    
    /**
     * 根据配置的根方法名，去解析上下文调用
     */
    @Override
    public void parser() {
        String rootMethodName = DataContext.javaCGConfInfo.getRootMethodName();
        rootMethodName = rootHandle(rootMethodName);
        if (DataContext.javaCGConfInfo.isRootMethodNext()) {
            nextParser(rootMethodName, 0);
        } else {
            prevParser(rootMethodName, 0);
        }
    }
    
    /**
     * 根方法的处理
     * @param rootMethodName 根方法名
     */
    protected abstract String rootHandle(String rootMethodName);
    
    /**
     * 向上解析
     * @param nextMethodName 被调用方法名
     * @param level 向上层级，从0开始
     */
    protected void prevParser(String nextMethodName, int level) {
        List<MethodCall> calleeList = DataContext.METHOD_CALLEE_MAP.get(nextMethodName);
        if (CollUtil.isEmpty(calleeList)) {
            METHOD_SET.remove(nextMethodName);
            return;
        }
        // 判断是否循环调用
        if (!METHOD_SET.add(nextMethodName)) {
            loopHandle(nextMethodName, level);
            return;
        }
    
        for (MethodCall prev : calleeList) {
            boolean handleResult = prevHandle(nextMethodName, prev, level + 1);
            if (! handleResult) {
                continue;
            }
            try {
                prevParser(prev.genCallerFullMethod(), level + 1);
            } catch (Throwable e) {
                // 层级过深，可能堆栈溢出
                System.out.println(nextMethodName);
                METHOD_SET.remove(nextMethodName);
                return;
            }
        }
        METHOD_SET.remove(nextMethodName);
    }
    
    /**
     * 循环调用处理，默认不处理
     * @param methodName 循环调用的名字
     * @param level 调用层级
     */
    protected void loopHandle(String methodName, int level) {};
    
    /**
     * 向上解析调用链路
     * @param nextMethodName 被调方的全名
     * @param prev 调用方的处理
     * @param level 调用层级，0开始
     * @return 是否解析成功，成功才继续向上解析
     */
    protected abstract boolean prevHandle(String nextMethodName, MethodCall prev, int level);
    
    /**
     * 向下解析
     */
    protected void nextParser(String prevMethodName, int level) {
        List<MethodCall> callList = DataContext.METHOD_CALL_MAP.get(prevMethodName);
        if (CollUtil.isEmpty(callList)) {
            METHOD_SET.remove(prevMethodName);
            return;
        }
        if (!METHOD_SET.add(prevMethodName)) {
            loopHandle(prevMethodName, level);
            return;
        }
        
        for (MethodCall next : callList) {
            boolean handleResult = nextHandle(prevMethodName, next, level + 1);
            if (! handleResult) {
                continue;
            }
            try {
                nextParser(next.genCalleeFullMethod(), level + 1);
            } catch (Throwable e) {
                // 层级过深，可能堆栈溢出
                System.out.println(prevMethodName);
                METHOD_SET.remove(prevMethodName);
                return;
            }
        }
        METHOD_SET.remove(prevMethodName);
    }
    
    /**
     * 向下解析调用链路
     * @param prevMethodName 上一个调用的全名
     * @param next 被调方的处理
     * @param level 调用层级，0开始
     * @return 是否解析成功，成功才继续向下解析
     */
    protected abstract boolean nextHandle(String prevMethodName, MethodCall next, int level);
    
    /**
     * 解析所有无接口调用的数据
     */
    public void praserAll() {
        Set<String> keySet = DataContext.METHOD_CALL_MAP.keySet();
        List<String> topMethod = keySet.stream()
                .filter(key -> !key.contains(":<init>")
                        && ObjectUtil.isNull(DataContext.METHOD_CALLEE_MAP.get(key)))
                .collect(Collectors.toList());
        for (String rootMethodName:topMethod) {
            this.nextParser(rootMethodName, 0);
        }
    }
}
