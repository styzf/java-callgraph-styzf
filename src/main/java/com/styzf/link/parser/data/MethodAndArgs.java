package com.styzf.link.parser.data;

import com.styzf.link.parser.util.JavaCGMethodUtil;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.generic.Type;

import java.util.Objects;

/**
 * 方法名及方法参数
 * @author styzf
 * @date 2023-09-26
 */
public class MethodAndArgs {
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数
     */
    private String methodArgs;

    private AccessFlags accessFlags;
    
    /**
     * 是否有具体的方法实现
     */
    private boolean done = true;
    
    /**
     * 是否有定义
     */
    private boolean define = true;
    
    public MethodAndArgs(String className, String methodName, Type[] argTypes, int accessFlags) {
        this(className, methodName, JavaCGMethodUtil.getArgListStr(argTypes), accessFlags);
    }

    public MethodAndArgs(String className, String methodName, String methodArgs, int accessFlags) {
        this.className = className;
        this.methodName = methodName;
        this.methodArgs = methodArgs;
        setAccessFlags(accessFlags);
        setDone();
    }

    private void setDone() {
        this.done = ! this.accessFlags.isAbstract();
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
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public boolean isDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
    
    public void setAccessFlags(int accessFlags) {
        this.accessFlags = new AccessFlags(accessFlags) {};
    }
    
    public AccessFlags getAccessFlags() {
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
    
    public boolean isDefine() {
        return define;
    }
    
    public void setDefine(boolean define) {
        this.define = define;
    }
}
