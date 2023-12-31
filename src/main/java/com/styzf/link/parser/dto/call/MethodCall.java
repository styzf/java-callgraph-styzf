package com.styzf.link.parser.dto.call;

import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.common.enums.JavaCGCallTypeEnum;
import com.styzf.link.parser.common.enums.JavaCGCalleeObjTypeEnum;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.util.JavaCGMethodUtil;

/**
 * @author adrninistrator
 * @date 2022/9/20
 * @description: 方法之间调用关系
 */
public class MethodCall implements Comparable<MethodCall> {

    // 方法调用序号
    private int callId;

    // 调用者类名
    private final String callerClassName;

    // 调用者方法名
    private final String callerMethodName;

    // 调用者方法参数
    private final String callerMethodArgs;

    // 方法调用类型
    private final JavaCGCallTypeEnum methodCallType;

    // 被调用者类名
    private final String calleeClassName;

    // 被调用者方法名
    private final String calleeMethodName;

    // 被调用者方法参数
    private final String calleeMethodArgs;

    // 调用者源代码行号
    private final int callerSourceLine;

    // 被调用类型
    private final JavaCGCalleeObjTypeEnum objTypeEnum;

    // 原始返回类型
    private final String rawReturnType;

    // 实际返回类型
    private final String actualReturnType;

    public MethodCall(int callId,
                      String callerClassName,
                      String callerMethodName,
                      String callerMethodArgs,
                      JavaCGCallTypeEnum methodCallType,
                      String calleeClassName,
                      String calleeMethodName,
                      String calleeMethodArgs,
                      int callerSourceLine,
                      JavaCGCalleeObjTypeEnum objTypeEnum,
                      String rawReturnType,
                      String actualReturnType
    ) {
        this(callerClassName,
                callerMethodName,
                callerMethodArgs,
                methodCallType,
                calleeClassName,
                calleeMethodName,
                calleeMethodArgs,
                callerSourceLine,
                objTypeEnum,
                rawReturnType,
                actualReturnType
        );
        this.callId = callId;
    }

    public MethodCall(String callerClassName,
                      String callerMethodName,
                      String callerMethodArgs,
                      JavaCGCallTypeEnum methodCallType,
                      String calleeClassName,
                      String calleeMethodName,
                      String calleeMethodArgs,
                      int callerSourceLine,
                      JavaCGCalleeObjTypeEnum objTypeEnum,
                      String rawReturnType,
                      String actualReturnType
    ) {
        this.callerClassName = callerClassName;
        this.callerMethodName = callerMethodName;
        this.callerMethodArgs = callerMethodArgs;
        this.methodCallType = methodCallType;
        this.calleeClassName = calleeClassName;
        this.calleeMethodName = calleeMethodName;
        this.calleeMethodArgs = calleeMethodArgs;
        this.callerSourceLine = callerSourceLine;
        this.objTypeEnum = objTypeEnum;
        this.rawReturnType = rawReturnType;
        this.actualReturnType = actualReturnType;
        DataContext.putMethodCallMap(this);
    }

    // 返回调用者完整方法
    public String genCallerFullMethod() {
        return JavaCGMethodUtil.formatFullMethod(callerClassName, callerMethodName, callerMethodArgs);
    }

    // 返回被调用类型对应的字符串
    public String genObjTypeEnum() {
        if (objTypeEnum == null) {
            return "";
        }
        return objTypeEnum.getType();
    }

    // 返回被调用者完整方法
    public String genCalleeFullMethod() {
        return JavaCGMethodUtil.formatFullMethod(calleeClassName, calleeMethodName, calleeMethodArgs);
    }

    // 生成在调用关系文件中的内容
    public String genCallContent(String callerJarNum, String calleeJarNum) {
        return StrUtil.join(JavaCGConstants.FILE_COLUMN_SEPARATOR,
                callId,
                genCallerFullMethod(),
                JavaCGConstants.FILE_KEY_CALL_TYPE_FLAG1 + methodCallType.getType() + JavaCGConstants.FILE_KEY_CALL_TYPE_FLAG2 + genCalleeFullMethod(),
                callerSourceLine,
                genObjTypeEnum(),
                rawReturnType,
                actualReturnType,
                callerJarNum,
                calleeJarNum
        );
    }

    public int getCallId() {
        return callId;
    }

    public void setCallId(int callId) {
        this.callId = callId;
    }

    public String getCalleeClassName() {
        return calleeClassName;
    }
    
    public String getCallerClassName() {
        return callerClassName;
    }
    
    @Override
    public int compareTo(MethodCall o) {
        return Integer.compare(this.callId, o.callId);
    }
}
