package com.styzf.link.parser.data;

import cn.hutool.core.collection.CollUtil;

import java.util.List;

/**
 *
 * 类涉及继承的信息，包含类的accessFlags，父类，及类中的方法信息
 * @author styzf
 * @date 2023-09-26
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
    private final List<MethodAndArgs> methodWithArgsList;

    public ClassExtendsMethodInfo(int accessFlags, String superClassName, List<MethodAndArgs> methodWithArgsList) {
        this.accessFlags = accessFlags;
        this.superClassName = superClassName;
        this.methodWithArgsList = methodWithArgsList;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public List<MethodAndArgs> getMethodWithArgsList() {
        return methodWithArgsList;
    }
    
    public void putMethodAndArgs(String className, MethodAndArgs methodAndArgs, int accessFlags) {
        MethodAndArgs one = CollUtil.findOne(methodWithArgsList, methodWithArgs -> methodWithArgs.equals(methodAndArgs));
        if (one == null) {
            // 为空则没有对应的数据，则新创建一个类，并标识为虚处理，这个虚处理待验证
            MethodAndArgs methodAndArgsNew = new MethodAndArgs(className, methodAndArgs.getMethodName(), methodAndArgs.getMethodArgs(), accessFlags);
            methodAndArgsNew.setDone(false);
            methodAndArgsNew.setDefine(false);
            methodAndArgsNew.setAccessFlags(accessFlags);
            methodWithArgsList.add(methodAndArgsNew);
        }
    }
}
