package com.styzf.link.parser.dto.classes;

import com.styzf.link.parser.data.MethodAndArgs;

import java.util.Map;

/**
 * @author adrninistrator
 * @date 2021/6/25
 * @description: 类涉及继承的信息，包含类的accessFlags，父类，及类中的方法信息
 */
public class ClassExtendsMethodInfo {
    // 类的的accessFlags
    private final int accessFlags;

    // 父类名称
    private final String superClassName;

    /*
        类的方法信息及accessFlags
        key
            方法信息
        value
            方法的accessFlags
     */
    private final Map<MethodAndArgs, Integer> methodWithArgsMap;

    public ClassExtendsMethodInfo(int accessFlags, String superClassName, Map<MethodAndArgs, Integer> methodWithArgsMap) {
        this.accessFlags = accessFlags;
        this.superClassName = superClassName;
        this.methodWithArgsMap = methodWithArgsMap;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public Map<MethodAndArgs, Integer> getMethodWithArgsMap() {
        return methodWithArgsMap;
    }
    
    public void putMethodAndArgs(String className, MethodAndArgs methodAndArgs, int accessFlags) {
        Integer flag = methodWithArgsMap.get(methodAndArgs);
        if (flag == null) {
            // 为空则没有对应的数据，则新创建一个类，并标识为虚处理
            MethodAndArgs methodAndArgsNew = new MethodAndArgs(className, methodAndArgs.getMethodName(), methodAndArgs.getMethodArgs(), accessFlags);
            methodAndArgsNew.setDone(false);
            methodAndArgsNew.setAccessFlags(accessFlags);
            methodWithArgsMap.putIfAbsent(methodAndArgsNew, accessFlags);
        } else {
            // 添加时不覆盖现有的值
            methodWithArgsMap.putIfAbsent(methodAndArgs, accessFlags);
        }
    }
}
