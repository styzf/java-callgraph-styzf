package com.styzf.link.parser.dto.call;

import com.styzf.link.parser.dto.counter.JavaCGCounter;

import java.util.ArrayList;
import java.util.List;

import static com.styzf.link.parser.context.CounterContext.CALL_ID_COUNTER;

/**
 * @author adrninistrator
 * @date 2023/2/7
 * @description: 方法之间调用关系列表
 */
public class MethodCallList {

    // 保存方法之间调用关系
    private final List<MethodCall> methodCallList = new ArrayList<>(50);

    // 保存方法调用计数器
    public void addMethodCall(MethodCall methodCall) {
        methodCall.setCallId(CALL_ID_COUNTER.addAndGet());
        methodCallList.add(methodCall);
    }

    public List<MethodCall> getMethodCallList() {
        return methodCallList;
    }
}
