package com.styzf.link.parser.data;

import com.styzf.link.parser.dto.method.MethodAndArgs;

import java.util.List;

/**
 * 类实现的接口，及类中的方法信息
 * @author styzf
 * @date 2023-09-26
 */
public class ClassImplementsMethodInfo {
    
    /**
     * 接口的类实现信息
     */
    private final List<MethodAndArgs> methodWithArgsList;
    
    private String className;

    public ClassImplementsMethodInfo(List<MethodAndArgs> methodWithArgsList) {
        this.methodWithArgsList = methodWithArgsList;
    }

    public List<MethodAndArgs> getMethodWithArgsList() {
        return methodWithArgsList;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
}
