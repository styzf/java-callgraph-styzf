package com.styzf.link.parser.parser;

import com.styzf.link.parser.conf.JavaCGConfInfo;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.classes.ClassExtendsMethodInfo;
import com.styzf.link.parser.dto.interfaces.InterfaceExtendsMethodInfo;
import com.styzf.link.parser.dto.method.MethodAndArgs;
import com.styzf.link.parser.spring.UseSpringBeanByAnnotationHandler;
import com.styzf.link.parser.util.JavaCGByteCodeUtil;
import com.styzf.link.parser.util.JavaCGUtil;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import static com.styzf.link.parser.context.DataContext.CLASS_EXTENDS_METHOD_INFO_MAP;
import static com.styzf.link.parser.context.DataContext.CLASS_EXTENDS_SET;
import static com.styzf.link.parser.context.DataContext.javaCGConfInfo;

/**
 * @author adrninistrator
 * @date 2022/9/14
 * @description: 解析jar包中的文件，第二次预处理
 */
public class JarEntryPreHandle2Parser extends AbstractJarEntryParser {

    private final UseSpringBeanByAnnotationHandler useSpringBeanByAnnotationHandler;

    public JarEntryPreHandle2Parser(UseSpringBeanByAnnotationHandler useSpringBeanByAnnotationHandler) {
        this.useSpringBeanByAnnotationHandler = useSpringBeanByAnnotationHandler;
    }

    @Override
    protected boolean handleEntry(JarInputStream jarInputStream, String jarEntryName) throws IOException {
        // 尝试处理jar包中的class文件
        tryHandleClassEntry(jarInputStream, jarEntryName);
        // 以上方法返回值不处理
        return true;
    }

    @Override
    protected boolean handleClassEntry(JavaClass javaClass, String jarEntryName) {
        if (javaClass.isClass()) {
            // 处理类
            // 查找涉及继承的类的信息，需要提前执行，使后续处理方法调用时，DataContext.CLASS_EXTENDS_METHOD_INFO_MAP的数据是完整的
            findClassExtendsInfo(javaClass);

            if (javaCGConfInfo.isParseMethodCallTypeValue()) {
                // 记录类中带有Spring相关注解的字段信息
                useSpringBeanByAnnotationHandler.recordClassFieldsWithSpringAnnotation(javaClass);
            }
            return true;
        }

        // 处理接口
        findInterfaceExtendsInfo(javaClass);
        return true;
    }

    // 查找涉及继承的类的信息
    private void findClassExtendsInfo(JavaClass javaClass) {
        String className = javaClass.getClassName();
        if (!CLASS_EXTENDS_SET.contains(className) || CLASS_EXTENDS_METHOD_INFO_MAP.get(className) != null) {
            // 假如当前类不涉及继承，或当前类已处理过，则不处理
            return;
        }

        String superClassName = javaClass.getSuperclassName();
        if (!JavaCGUtil.isClassInJdk(superClassName)) {
            // 记录父类及其子类，忽略以"java."开头的父类
            List<String> childrenClassList = DataContext.CHILDREN_CLASS_MAP.computeIfAbsent(superClassName, k -> new ArrayList<>());
            childrenClassList.add(className);
        }

        Map<MethodAndArgs, Integer> methodAttributeMap = new HashMap<>();
        // 遍历类的方法
        for (Method method : javaClass.getMethods()) {
            String methodName = method.getName();
            if (JavaCGByteCodeUtil.checkExtendsMethod(methodName, method)) {
                // 对于可能涉及继承的方法进行记录
                methodAttributeMap.put(new MethodAndArgs(methodName, method.getArgumentTypes()), method.getAccessFlags());
            }
        }
        CLASS_EXTENDS_METHOD_INFO_MAP.put(className, new ClassExtendsMethodInfo(javaClass.getAccessFlags(), superClassName, methodAttributeMap));
    }

    // 查找涉及继承的接口的信息
    private void findInterfaceExtendsInfo(JavaClass interfaceClass) {
        String interfaceName = interfaceClass.getClassName();
        if (interfaceClass.isAnnotation() ||
                !DataContext.INTERFACE_EXTENDS_SET.contains(interfaceName) ||
                DataContext.INTERFACE_EXTENDS_METHOD_INFO_MAP.get(interfaceName) != null) {
            // 假如为水底有，或当前接口不涉及继承，或当前接口已处理过，则不处理
            return;
        }

        String[] superInterfaceNames = interfaceClass.getInterfaceNames();
        for (String superInterfaceName : superInterfaceNames) {
            // 记录父类及其子类，忽略以"java."开头的父类
            List<String> childrenInterfaceList = DataContext.CHILDREN_INTERFACE_MAP.computeIfAbsent(superInterfaceName, k -> new ArrayList<>());
            childrenInterfaceList.add(interfaceName);
        }

        // 记录当前接口的方法信息
        List<MethodAndArgs> methodAttributeList = new ArrayList<>();
        for (Method method : interfaceClass.getMethods()) {
            methodAttributeList.add(new MethodAndArgs(method.getName(), method.getArgumentTypes()));
        }
        DataContext.INTERFACE_EXTENDS_METHOD_INFO_MAP.put(interfaceName, new InterfaceExtendsMethodInfo(Arrays.asList(superInterfaceNames), methodAttributeList));
    }
}
