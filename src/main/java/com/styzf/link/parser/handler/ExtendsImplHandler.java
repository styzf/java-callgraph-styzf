package com.styzf.link.parser.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.common.enums.JavaCGCallTypeEnum;
import com.styzf.link.parser.comparator.MethodAndArgsComparator;
import com.styzf.link.parser.conf.JavaCGConfInfo;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.access_flag.JavaCGAccessFlags;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.classes.ClassExtendsMethodInfo;
import com.styzf.link.parser.dto.classes.ClassImplementsMethodInfo;
import com.styzf.link.parser.dto.classes.Node4ClassExtendsMethod;
import com.styzf.link.parser.dto.interfaces.InterfaceExtendsMethodInfo;
import com.styzf.link.parser.dto.method.MethodAndArgs;
import com.styzf.link.parser.dto.stack.ListAsStack;
import com.styzf.link.parser.util.JavaCGByteCodeUtil;
import com.styzf.link.parser.util.JavaCGLogUtil;
import com.styzf.link.parser.util.JavaCGMethodUtil;
import com.styzf.link.parser.util.JavaCGUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.styzf.link.parser.common.JavaCGConstants.EXTENDS_NODE_INDEX_INIT;
import static com.styzf.link.parser.context.CounterContext.CALL_ID_COUNTER;
import static com.styzf.link.parser.context.DataContext.CHILDREN_CLASS_MAP;
import static com.styzf.link.parser.context.DataContext.CHILDREN_INTERFACE_MAP;
import static com.styzf.link.parser.context.DataContext.CLASS_AND_JAR_NUM;
import static com.styzf.link.parser.context.DataContext.CLASS_EXTENDS_METHOD_INFO_MAP;
import static com.styzf.link.parser.context.DataContext.CLASS_IMPLEMENTS_METHOD_INFO_MAP;
import static com.styzf.link.parser.context.DataContext.INTERFACE_EXTENDS_METHOD_INFO_MAP;
import static com.styzf.link.parser.context.DataContext.INTERFACE_METHOD_NONE_DONE_MAP;
import static com.styzf.link.parser.context.DataContext.INTERFACE_METHOD_WITH_ARGS_MAP;
import static java.lang.System.err;

/**
 * @author adrninistrator
 * @date 2022/11/13
 * @description 继承及实现相关的方法处理类
 */
public class ExtendsImplHandler {
    private JavaCGConfInfo javaCGConfInfo;

    public void handle() {
        addSuperInterfaceMethod4ChildrenInterface();
        addInterfaceMethod4SuperAbstractClass();
        recordClassExtendsMethod();
        recordInterfaceCallClassMethod();
        recordNoneDoneMethod();
    }
    
    /**
     * 记录没有实际方法实现的方法，向上查找是否有接口实现
     */
    private void recordNoneDoneMethod() {
        for (Map.Entry<String, List<MethodAndArgs>> entry : INTERFACE_METHOD_NONE_DONE_MAP.entrySet()) {
            String className = entry.getKey();
            List<MethodAndArgs> methodAndArgsList = entry.getValue();
            ClassImplementsMethodInfo interfaceClassImplementsMethodInfo = getInterfaceClassImplementsMethodInfo(className, methodAndArgsList, className);
            
            if (interfaceClassImplementsMethodInfo == null) {
                continue;
            }
            
            iteratorAllInterface(className, methodAndArgsList, interfaceClassImplementsMethodInfo);
        }
    }
    
    /**
     * TODO 该数据结构无法满足要求
     * 遍历所有的接口
     * @param className 类名
     * @param methodAndArgsList 需要解析的方法列表
     * @param interfaceClassImplementsMethodInfo 接口以及实现
     */
    private void iteratorAllInterface(String className, List<MethodAndArgs> methodAndArgsList, ClassImplementsMethodInfo interfaceClassImplementsMethodInfo) {
        List<MethodAndArgs> methodWithArgsList = interfaceClassImplementsMethodInfo.getMethodWithArgsList();
        Iterator<MethodAndArgs> iterator = methodAndArgsList.iterator();
        while (iterator.hasNext()) {
            MethodAndArgs methodAndArgs = iterator.next();
            MethodAndArgs interfaceMethodAndArgs = JavaCGMethodUtil.getByQuery(methodWithArgsList, methodAndArgs);
            if (interfaceMethodAndArgs == null
                    || !interfaceMethodAndArgs.isDone()) {
                continue;
            }
            // 接口有默认实现
            addExtraMethodCall(className,
                    methodAndArgs.getMethodName(),
                    methodAndArgs.getMethodArgs(),
                    JavaCGCallTypeEnum.CTE_CHILD_CALL_SUPER,
                    interfaceClassImplementsMethodInfo.getInterfaceNameList().get(0),
                    interfaceMethodAndArgs.getMethodName(),
                    interfaceMethodAndArgs.getMethodArgs());
            iterator.remove();
        }
    }
    
    /**
     * 获取接口方法
     * @param className 类名
     * @param methodAndArgsList 要查找的方法
     * @param sourceClassName 原始类名
     * @return 接口方法
     */
    private ClassImplementsMethodInfo getInterfaceClassImplementsMethodInfo(String className, List<MethodAndArgs> methodAndArgsList, String sourceClassName) {
        ClassImplementsMethodInfo classImplementsMethodInfo = CLASS_IMPLEMENTS_METHOD_INFO_MAP.get(className);
        if (classImplementsMethodInfo != null) {
            return classImplementsMethodInfo;
        }
        
        ClassExtendsMethodInfo classExtendsMethodInfo = CLASS_EXTENDS_METHOD_INFO_MAP.get(className);
        if (ObjectUtil.isNull(classExtendsMethodInfo) ||
                StrUtil.isBlank(classExtendsMethodInfo.getSuperClassName())) {
            return null;
        }
        
        Iterator<MethodAndArgs> iterator = methodAndArgsList.iterator();
        while (iterator.hasNext()) {
            MethodAndArgs methodAndArgs = iterator.next();
            MethodAndArgs superMethodAndArgs = JavaCGMethodUtil.getByQuery(classExtendsMethodInfo.getMethodWithArgsMap(), methodAndArgs);
            if (superMethodAndArgs == null
                    || !superMethodAndArgs.isDone()) {
                continue;
            }
            // 父类有具体的实现
            addExtraMethodCall(sourceClassName,
                    methodAndArgs.getMethodName(),
                    methodAndArgs.getMethodArgs(),
                    JavaCGCallTypeEnum.CTE_CHILD_CALL_SUPER,
                    classExtendsMethodInfo.getSuperClassName(),
                    superMethodAndArgs.getMethodName(),
                    superMethodAndArgs.getMethodArgs());
            iterator.remove();
        }
        
        return getInterfaceClassImplementsMethodInfo(classExtendsMethodInfo.getSuperClassName(), methodAndArgsList, sourceClassName);
    }
    
    /**
     * 将父接口中的方法添加到子接口中
     */
    private void addSuperInterfaceMethod4ChildrenInterface() {
        // 查找顶层父接口
        Set<String> topSuperInterfaceSet = new HashSet<>();
        for (Map.Entry<String, InterfaceExtendsMethodInfo> entry : INTERFACE_EXTENDS_METHOD_INFO_MAP.entrySet()) {
            InterfaceExtendsMethodInfo interfaceExtendsMethodInfo = entry.getValue();
            for (String superInterface : interfaceExtendsMethodInfo.getSuperInterfaceList()) {
                InterfaceExtendsMethodInfo superInterfaceExtendsMethodInfo = INTERFACE_EXTENDS_METHOD_INFO_MAP.get(superInterface);
                if (superInterfaceExtendsMethodInfo == null || superInterfaceExtendsMethodInfo.getSuperInterfaceList().isEmpty()) {
                    // 父接口在接口涉及继承的信息Map中不存在记录，或父接口列表为空，说明当前为顶层父接口
                    if (!topSuperInterfaceSet.add(superInterface)) {
                        continue;
                    }
                    if (JavaCGLogUtil.isDebugPrintFlag()) {
                        JavaCGLogUtil.debugPrint("处理一个顶层父接口: " + superInterface);
                    }
                }
            }
        }

        List<String> topSuperInterfaceSetList = new ArrayList<>(topSuperInterfaceSet);
        // 对顶层父接口类名排序
        Collections.sort(topSuperInterfaceSetList);
        for (String topSuperInterface : topSuperInterfaceSetList) {
            // 遍历顶层父接口并处理
            handleOneSuperInterface(topSuperInterface);
        }
    }

    // 处理一个父接口
    private void handleOneSuperInterface(String superInterface) {
        List<String> childrenInterfaceList = CHILDREN_INTERFACE_MAP.get(superInterface);
        if (childrenInterfaceList == null) {
            return;
        }

        for (String childrenInterface : childrenInterfaceList) {
            // 处理父接口及一个子接口
            handleSuperAndChildInterface(superInterface, childrenInterface);

            // 继续处理子接口
            handleOneSuperInterface(childrenInterface);
        }
    }

    // 处理父接口及一个子接口
    private void handleSuperAndChildInterface(String superInterface, String childInterface) {
        InterfaceExtendsMethodInfo superInterfaceExtendsMethodInfo = INTERFACE_EXTENDS_METHOD_INFO_MAP.get(superInterface);
        if (superInterfaceExtendsMethodInfo == null) {
            // 父接口在接口涉及继承的信息Map中不存在记录时，不处理
            return;
        }

        InterfaceExtendsMethodInfo childInterfaceExtendsMethodInfo = INTERFACE_EXTENDS_METHOD_INFO_MAP.get(childInterface);

        List<MethodAndArgs> superInterfaceMethodAndArgsList = superInterfaceExtendsMethodInfo.getMethodAndArgsList();
        // 对父接口中的方法进行排序
        superInterfaceMethodAndArgsList.sort(MethodAndArgsComparator.getInstance());
        // 遍历父接口中的方法
        for (MethodAndArgs superMethodAndArgs : superInterfaceMethodAndArgsList) {
            List<MethodAndArgs> childInterfaceMethodAndArgsList = childInterfaceExtendsMethodInfo.getMethodAndArgsList();
            if (childInterfaceMethodAndArgsList.contains(superMethodAndArgs)) {
                // 子接口中已包含父接口，跳过
                continue;
            }

            // 在子接口中添加父接口的方法（涉及继承的接口相关结构）
            childInterfaceMethodAndArgsList.add(superMethodAndArgs);

            // 在子接口中添加父接口的方法（所有接口都需要记录的结构）
            List<MethodAndArgs> childInterfaceMethodAndArgsListAll = INTERFACE_METHOD_WITH_ARGS_MAP.computeIfAbsent(childInterface, k -> new ArrayList<>());
            childInterfaceMethodAndArgsListAll.add(superMethodAndArgs);

            // 添加子接口调用父接口方法
            addExtraMethodCall(childInterface, superMethodAndArgs.getMethodName(), superMethodAndArgs.getMethodArgs(),
                    JavaCGCallTypeEnum.CTE_CHILD_CALL_SUPER_INTERFACE, superInterface, superMethodAndArgs.getMethodName(), superMethodAndArgs.getMethodArgs());
        }
    }
    
    /**
     * 将接口中的抽象方法加到抽象父类中
     */
    private void addInterfaceMethod4SuperAbstractClass() {
        for (Map.Entry<String, List<String>> childrenClassEntry : CHILDREN_CLASS_MAP.entrySet()) {
            String superClassName = childrenClassEntry.getKey();
            ClassExtendsMethodInfo classExtendsMethodInfo = CLASS_EXTENDS_METHOD_INFO_MAP.get(superClassName);
            if (classExtendsMethodInfo == null || !JavaCGByteCodeUtil.isAbstractFlag(classExtendsMethodInfo.getAccessFlags())) {
                /*
                    为空的情况，对应其他jar包中的Class可以找到，但是找不到它们的方法，是正常的，不处理
                    若不是抽象类则不处理
                 */
                continue;
            }

            ClassImplementsMethodInfo classImplementsMethodInfo = CLASS_IMPLEMENTS_METHOD_INFO_MAP.get(superClassName);
            if (classImplementsMethodInfo == null) {
                continue;
            }

            Map<MethodAndArgs, Integer> methodWithArgsMap = classExtendsMethodInfo.getMethodWithArgsMap();

            int accessFlags = 0;
            accessFlags = JavaCGByteCodeUtil.setAbstractFlag(accessFlags, true);
            accessFlags = JavaCGByteCodeUtil.setPublicFlag(accessFlags, true);
            accessFlags = JavaCGByteCodeUtil.setProtectedFlag(accessFlags, false);

            // 将接口中的抽象方法加到抽象父类中
            for (String interfaceName : classImplementsMethodInfo.getInterfaceNameList()) {
                List<MethodAndArgs> interfaceMethodWithArgsList = INTERFACE_METHOD_WITH_ARGS_MAP.get(interfaceName);
                if (interfaceMethodWithArgsList == null) {
                    continue;
                }

                for (MethodAndArgs interfaceMethodWithArgs : interfaceMethodWithArgsList) {
                    classExtendsMethodInfo.putMethodAndArgs(interfaceMethodWithArgs, accessFlags);
                }
            }
        }
    }
    
    /**
     * 记录父类调用子类方法，及子类调用父类方法
     */
    private void recordClassExtendsMethod() {
        Set<String> topSuperClassNameSet = new HashSet<>();

        // 得到最顶层父类名称
        for (Map.Entry<String, ClassExtendsMethodInfo> classExtendsMethodInfoEntry : CLASS_EXTENDS_METHOD_INFO_MAP.entrySet()) {
            String className = classExtendsMethodInfoEntry.getKey();
            ClassExtendsMethodInfo classExtendsMethodInfo = classExtendsMethodInfoEntry.getValue();
            String superClassName = classExtendsMethodInfo.getSuperClassName();
            if (JavaCGUtil.isClassInJdk(superClassName)) {
                topSuperClassNameSet.add(className);
            }
        }

        List<String> topSuperClassNameList = new ArrayList<>(topSuperClassNameSet);
        // 对顶层父类类名排序
        Collections.sort(topSuperClassNameList);
        for (String topSuperClassName : topSuperClassNameList) {
            handleOneTopSuperClass(topSuperClassName);
        }
    }
    
    /**
     * 处理一个顶层父类
     * @param topSuperClassName 顶级父类名
     */
    private void handleOneTopSuperClass(String topSuperClassName) {
        if (JavaCGLogUtil.isDebugPrintFlag()) {
            JavaCGLogUtil.debugPrint("处理一个顶层父类: " + topSuperClassName);
        }
        ListAsStack<Node4ClassExtendsMethod> nodeStack = new ListAsStack<>();

        // 初始化节点列表
        Node4ClassExtendsMethod topNode = new Node4ClassExtendsMethod(topSuperClassName, EXTENDS_NODE_INDEX_INIT);
        nodeStack.push(topNode);

        // 开始循环
        while (true) {
            Node4ClassExtendsMethod currentNode = nodeStack.peek();
            List<String> childrenClassList = CHILDREN_CLASS_MAP.get(currentNode.getSuperClassName());
            if (childrenClassList == null) {
                err.println("### 未找到顶层父类的子类: " + currentNode.getSuperClassName());
                return;
            }

            // 对子类类名排序
            Collections.sort(childrenClassList);
            int currentChildClassIndex = currentNode.getChildClassIndex() + 1;
            if (currentChildClassIndex >= childrenClassList.size()) {
                if (nodeStack.atBottom()) {
                    return;
                }
                // 删除栈顶元素
                nodeStack.removeTop();
                continue;
            }

            // 处理当前的子类
            String childClassName = childrenClassList.get(currentChildClassIndex);

            // 处理父类和子类的方法调用
            handleSuperAndChildClass(currentNode.getSuperClassName(), childClassName);

            // 处理下一个子类
            currentNode.setChildClassIndex(currentChildClassIndex);

            List<String> nextChildClassList = CHILDREN_CLASS_MAP.get(childClassName);
            if (nextChildClassList == null) {
                // 当前的子类下没有子类
                continue;
            }

            // 当前的子类下有子类
            // 入栈
            Node4ClassExtendsMethod nextNode = new Node4ClassExtendsMethod(childClassName, EXTENDS_NODE_INDEX_INIT);
            nodeStack.push(nextNode);
        }
    }
    
    /**
     * 处理父类和子类的方法调用
     * @param superClassName 父类名
     * @param childClassName 子类名
     */
    private void handleSuperAndChildClass(String superClassName, String childClassName) {
        ClassExtendsMethodInfo superClassMethodInfo = CLASS_EXTENDS_METHOD_INFO_MAP.get(superClassName);
        if (superClassMethodInfo == null) {
            err.println("### 未找到父类信息: " + superClassName);
            return;
        }

        ClassExtendsMethodInfo childClassMethodInfo = CLASS_EXTENDS_METHOD_INFO_MAP.get(childClassName);
        if (childClassMethodInfo == null) {
            err.println("### 未找到子类信息: " + childClassName);
            return;
        }

        Map<MethodAndArgs, Integer> superMethodWithArgsMap = superClassMethodInfo.getMethodWithArgsMap();
        Map<MethodAndArgs, Integer> childMethodWithArgsMap = childClassMethodInfo.getMethodWithArgsMap();

        List<MethodAndArgs> superMethodAndArgsList = new ArrayList<>(superMethodWithArgsMap.keySet());
        List<MethodAndArgs> noneDoneList = new LinkedList<>();
        // 对父类方法进行排序
        superMethodAndArgsList.sort(MethodAndArgsComparator.getInstance());
        // 遍历父类方法
        for (MethodAndArgs superMethodWithArgs : superMethodAndArgsList) {
            Integer superMethodAccessFlags = superMethodWithArgsMap.get(superMethodWithArgs);
            if (JavaCGByteCodeUtil.isAbstractFlag(superMethodAccessFlags)) {
                // 处理父类抽象方法
                // 添加时不覆盖现有的值
                childClassMethodInfo.putMethodAndArgs(superMethodWithArgs, superMethodAccessFlags);
                // 添加父类调用子类的方法调用
                addExtraMethodCall(superClassName, superMethodWithArgs.getMethodName(), superMethodWithArgs.getMethodArgs(),
                        JavaCGCallTypeEnum.CTE_SUPER_CALL_CHILD, childClassName, superMethodWithArgs.getMethodName(), superMethodWithArgs.getMethodArgs());
                MethodAndArgs childMethodWithArgs = JavaCGMethodUtil.getByQuery(childMethodWithArgsMap, superMethodWithArgs);
                if (!superMethodWithArgs.isDone()
                        && childMethodWithArgs != null
                        && !childMethodWithArgs.isDone()) {
                    noneDoneList.add(childMethodWithArgs);
                }
                continue;
            }

            if (JavaCGByteCodeUtil.isPublicFlag(superMethodAccessFlags)
                    || JavaCGByteCodeUtil.isProtectedMethod(superMethodAccessFlags)
                    || (!JavaCGByteCodeUtil.isPrivateMethod(superMethodAccessFlags)
                    && JavaCGUtil.checkSamePackage(superClassName, childClassName))
            ) {
                /*
                    对于父类中满足以下条件的非抽象方法进行处理：
                    public
                    或protected
                    或非public非protected非private且父类与子类在同一个包
                 */
                if (childMethodWithArgsMap.get(superMethodWithArgs) != null) {
                    // 若当前方法已经处理过则跳过
                    continue;
                }

                childClassMethodInfo.putMethodAndArgs(superMethodWithArgs, superMethodAccessFlags);
                // 添加子类调用父类方法
                addExtraMethodCall(childClassName, superMethodWithArgs.getMethodName(), superMethodWithArgs.getMethodArgs(),
                        JavaCGCallTypeEnum.CTE_CHILD_CALL_SUPER, superClassName, superMethodWithArgs.getMethodName(), superMethodWithArgs.getMethodArgs());
            }
        }
        
        if (CollUtil.isNotEmpty(noneDoneList)) {
            INTERFACE_METHOD_NONE_DONE_MAP.put(childClassName, noneDoneList);
        }
    }
    
    /**
     * 记录接口调用实现类方法
     */
    private void recordInterfaceCallClassMethod() {
        if (CLASS_IMPLEMENTS_METHOD_INFO_MAP.isEmpty() || INTERFACE_METHOD_WITH_ARGS_MAP.isEmpty()) {
            return;
        }

        List<String> classNameList = new ArrayList<>(CLASS_IMPLEMENTS_METHOD_INFO_MAP.keySet());
        // 对类名进行排序
        Collections.sort(classNameList);
        // 对类名进行遍历
        for (String className : classNameList) {
            ClassImplementsMethodInfo classImplementsMethodInfo = CLASS_IMPLEMENTS_METHOD_INFO_MAP.get(className);
            List<String> interfaceNameList = classImplementsMethodInfo.getInterfaceNameList();
            // 对实现的接口进行排序
            Collections.sort(interfaceNameList);

            // 找到在接口和实现类中都存在的方法
            for (String interfaceName : interfaceNameList) {
                List<MethodAndArgs> interfaceMethodWithArgsList = INTERFACE_METHOD_WITH_ARGS_MAP.get(interfaceName);
                if (JavaCGUtil.isCollectionEmpty(interfaceMethodWithArgsList)) {
                    continue;
                }

                List<MethodAndArgs> methodWithArgsList = classImplementsMethodInfo.getMethodWithArgsList();
                // 在处理接口调用实现类方法时，将父类中定义的可能涉及实现的方法添加到当前类的方法中
                addSuperMethod2ImplClass(methodWithArgsList, className);

                // 对方法进行排序
                methodWithArgsList.sort(MethodAndArgsComparator.getInstance());
                // 对方法进行遍历
                for (MethodAndArgs methodWithArgs : methodWithArgsList) {
                    if (!interfaceMethodWithArgsList.contains(methodWithArgs)) {
                        // 接口中不包含的方法，跳过
                        continue;
                    }
                    // 添加接口调用实现类方法
                    addExtraMethodCall(interfaceName, methodWithArgs.getMethodName(), methodWithArgs.getMethodArgs(),
                            JavaCGCallTypeEnum.CTE_INTERFACE_CALL_IMPL_CLASS, className, methodWithArgs.getMethodName(), methodWithArgs.getMethodArgs());
                }
            }
        }
    }

    /**
     * 在处理接口调用实现类方法时，将父类中定义的可能涉及实现的方法添加到当前类的方法中
     *
     * @param methodWithArgsList 当前类中定义的方法
     * @param className
     */
    private void addSuperMethod2ImplClass(List<MethodAndArgs> methodWithArgsList, String className) {
        // 获取当前处理的实现类涉及继承的信息
        ClassExtendsMethodInfo classExtendsMethodInfo = CLASS_EXTENDS_METHOD_INFO_MAP.get(className);
        if (classExtendsMethodInfo == null) {
            return;
        }

        // 获取当前处理的实现类中的方法信息
        Map<MethodAndArgs, Integer> methodWithArgsMap = classExtendsMethodInfo.getMethodWithArgsMap();
        if (methodWithArgsMap == null) {
            return;
        }

        for (Map.Entry<MethodAndArgs, Integer> entry : methodWithArgsMap.entrySet()) {
            MethodAndArgs methodAndArgs = entry.getKey();
            if (methodWithArgsList.contains(methodAndArgs)) {
                // 已包含的方法，跳过
                continue;
            }

            String methodName = methodAndArgs.getMethodName();
            JavaCGAccessFlags methodAccessFlags = new JavaCGAccessFlags(entry.getValue());
            if (JavaCGByteCodeUtil.checkImplMethod(methodName, methodAccessFlags)) {
                // 将父类中定义的，可能涉及实现的方法添加到当前类的方法中
                methodWithArgsList.add(methodAndArgs);
            }
        }
    }

    // 添加额外的方法调用关系
    private void addExtraMethodCall(String callerClassName,
                                    String callerMethodName,
                                    String callerMethodArgs,
                                    JavaCGCallTypeEnum methodCallType,
                                    String calleeClassName,
                                    String calleeMethodName,
                                    String calleeMethodArgs) {
        if (JavaCGUtil.checkSkipClass(callerClassName, javaCGConfInfo.getNeedHandlePackageSet()) ||
                JavaCGUtil.checkSkipClass(calleeClassName, javaCGConfInfo.getNeedHandlePackageSet())) {
            return;
        }

        String callerClassJarNum = CLASS_AND_JAR_NUM.getJarNum(callerClassName);
        String calleeClassJarNum = CLASS_AND_JAR_NUM.getJarNum(calleeClassName);

        MethodCall methodCall = new MethodCall(
                CALL_ID_COUNTER.addAndGet(),
                callerClassName,
                callerMethodName,
                callerMethodArgs,
                methodCallType,
                calleeClassName,
                calleeMethodName,
                calleeMethodArgs,
                JavaCGConstants.DEFAULT_LINE_NUMBER,
                null,
                null,
                null
        );
    }

    public void setJavaCGConfInfo(JavaCGConfInfo javaCGConfInfo) {
        this.javaCGConfInfo = javaCGConfInfo;
    }
}
