package com.styzf.link.parser.dto.method;

import com.styzf.link.parser.util.JavaCGMethodUtil;
import org.apache.bcel.Const;
import org.apache.bcel.generic.Type;

import java.util.Objects;

/**
 * @author adrninistrator
 * @date 2022/10/1
 * @description: 方法名及方法参数
 */
public class MethodAndArgs {
    private String methodName;

    private String methodArgs;

    private int accessFlags;
    
    /**
     * 是否为实际调用方法，或者为虚方法，虚方法即为继承下来的方法
     */
    private boolean done = true;
    
    public MethodAndArgs(String methodName, Type[] argTypes) {
        this.methodName = methodName;
        this.methodArgs = JavaCGMethodUtil.getArgListStr(argTypes);
    }

    public MethodAndArgs(String methodName, String methodArgs) {
        this.methodName = methodName;
        this.methodArgs = methodArgs;
    }

    @Override
    public String toString() {
        return methodName + methodArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodAndArgs that = (MethodAndArgs) o;
        return Objects.equals(methodName, that.methodName) && Objects.equals(methodArgs, that.methodArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, methodArgs);
    }
    
    public boolean isDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
    
    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
    }
    
    public int getAccessFlags() {
        return accessFlags;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(String methodArgs) {
        this.methodArgs = methodArgs;
    }
}
