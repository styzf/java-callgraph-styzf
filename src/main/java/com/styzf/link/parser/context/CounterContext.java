package com.styzf.link.parser.context;

import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.dto.counter.JavaCGCounter;

/**
 * 计数器上下文
 * @author styzf
 * @date 2023/9/23 20:37
 */
public interface CounterContext {
    /**
     * 方法调用计数器
     */
    public final JavaCGCounter CALL_ID_COUNTER = new JavaCGCounter(JavaCGConstants.METHOD_CALL_ID_START);
    /**
     * 类处理量计数器
     */
    public final JavaCGCounter CLASS_NUM_COUNTER = new JavaCGCounter(JavaCGConstants.METHOD_CALL_ID_START);
    /**
     * 方法处理量计数器
     */
    public final JavaCGCounter METHOD_NUM_COUNTER = new JavaCGCounter(JavaCGConstants.METHOD_CALL_ID_START);
}
