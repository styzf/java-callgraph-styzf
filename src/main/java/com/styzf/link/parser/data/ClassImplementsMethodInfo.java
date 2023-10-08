package com.styzf.link.parser.data;

import cn.hutool.core.collection.CollUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类实现的接口，及类中的方法信息
 * @author styzf
 * @date 2023-09-26
 */
public class ClassImplementsMethodInfo {
    
    /**
     * 类实现的接口
     */
    private final List<String> interfaceNameList;
    /**
     * 接口的类实现信息
     */
    private final List<MethodAndArgs> methodWithArgsList;
    
    private String className;

    public ClassImplementsMethodInfo(List<String> interfaceNameList, List<MethodAndArgs> methodWithArgsList) {
        this.interfaceNameList = interfaceNameList;
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
    
    public List<String> getInterfaceNameList() {
        return interfaceNameList;
    }
}
