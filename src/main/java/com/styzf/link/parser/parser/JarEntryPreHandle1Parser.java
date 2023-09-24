package com.styzf.link.parser.parser;

import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.common.JavaCGCommonNameConstants;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.conf.JavaCGConfInfo;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.classes.ClassImplementsMethodInfo;
import com.styzf.link.parser.dto.method.MethodAndArgs;
import com.styzf.link.parser.extensions.code_parser.JarEntryOtherFileParser;
import com.styzf.link.parser.extensions.manager.ExtensionsManager;
import com.styzf.link.parser.spring.DefineSpringBeanByAnnotationHandler;
import com.styzf.link.parser.util.JavaCGByteCodeUtil;
import com.styzf.link.parser.util.JavaCGUtil;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;

import static com.styzf.link.parser.context.DataContext.javaCGConfInfo;

/**
 * @author adrninistrator
 * @date 2022/9/14
 * @description: 解析jar包中的文件，第一次预处理
 */
public class JarEntryPreHandle1Parser extends AbstractJarEntryParser {
    
    private final ExtensionsManager extensionsManager;

    private final DefineSpringBeanByAnnotationHandler defineSpringBeanByAnnotationHandler;

    public JarEntryPreHandle1Parser(DefineSpringBeanByAnnotationHandler defineSpringBeanByAnnotationHandler,
                                    ExtensionsManager extensionsManager) {
        this.defineSpringBeanByAnnotationHandler = defineSpringBeanByAnnotationHandler;
        this.extensionsManager = extensionsManager;
    }

    @Override
    protected boolean handleEntry(JarInputStream jarInputStream, String jarEntryName) throws IOException {
        // 尝试处理jar包中的class文件
        if (tryHandleClassEntry(jarInputStream, jarEntryName)) {
            // 是class文件，不再处理
            return true;
        }

        // 非class文件，判断是否需要使用扩展类处理jar包中的文件
        String jarEntryFileExt = StrUtil.subAfter(jarEntryName, JavaCGConstants.FLAG_DOT, true);
        List<JarEntryOtherFileParser> jarEntryOtherFileParserList = extensionsManager.getJarEntryOtherFileParserList(jarEntryFileExt);
        if (jarEntryOtherFileParserList == null) {
            // 当前文件不存在对应的扩展类，不处理
            return true;
        }

        // 存在扩展类需要处理当前文件
        // 将不可重复读的JarInputStream缓存为可以重复读取的ByteArrayInputStream
        InputStream cachedInputStream = JavaCGUtil.cacheInputStream(jarInputStream);
        if (cachedInputStream == null) {
            return false;
        }

        // 调用扩展类的方法
        for (JarEntryOtherFileParser jarEntryOtherFileParser : jarEntryOtherFileParserList) {
            // 处理一个jar包中的文件
            jarEntryOtherFileParser.parseJarEntryOtherFile(cachedInputStream, jarEntryName);
            // 重置缓存的InputStream，使下次能够从头开始继续读取
            cachedInputStream.reset();
        }

        return true;
    }

    @Override
    protected boolean handleClassEntry(JavaClass javaClass, String jarEntryName) {
        // 记录类名及所在的jar包序号
        DataContext.CLASS_AND_JAR_NUM.put(javaClass.getClassName(), lastJarInfo.getJarNum());

        if (javaClass.isInterface()) {
            // 对一个接口进行预处理
            preHandle1Interface(javaClass);
            return true;
        }

        // 对一个类进行预处理
        preHandle1Class(javaClass);

        if (javaCGConfInfo.isParseMethodCallTypeValue()) {
            // 处理Spring Bean相关注解
            return defineSpringBeanByAnnotationHandler.recordSpringBeanInfo(javaClass);
        }

        return true;
    }

    // 对一个接口进行预处理
    private void preHandle1Interface(JavaClass interfaceClass) {
        if (interfaceClass.isAnnotation()) {
            // 不处理注解
            return;
        }

        String interfaceName = interfaceClass.getClassName();
        // 记录接口的方法
        Method[] methods = interfaceClass.getMethods();
        if (methods != null && methods.length > 0 &&
                DataContext.INTERFACE_METHOD_WITH_ARGS_MAP.get(interfaceName) == null) {
            List<MethodAndArgs> interfaceMethodWithArgsList = JavaCGByteCodeUtil.genInterfaceMethodWithArgs(methods);
            DataContext.INTERFACE_METHOD_WITH_ARGS_MAP.put(interfaceName, interfaceMethodWithArgsList);
        }

        String[] superInterfaceNames = interfaceClass.getInterfaceNames();
        if (superInterfaceNames.length > 0) {
            // 记录涉及继承的接口
            DataContext.INTERFACE_EXTENDS_SET.add(interfaceName);
            DataContext.INTERFACE_EXTENDS_SET.addAll(Arrays.asList(superInterfaceNames));
        }
    }

    // 对一个Class进行预处理
    private void preHandle1Class(JavaClass javaClass) {
        String className = javaClass.getClassName();
        String[] interfaceNames = javaClass.getInterfaceNames();
        Method[] methods = javaClass.getMethods();

        if (interfaceNames.length > 0 &&
                methods != null && methods.length > 0 &&
                DataContext.CLASS_IMPLEMENTS_METHOD_INFO_MAP.get(className) == null) {
            List<String> interfaceNameList = new ArrayList<>(interfaceNames.length);
            interfaceNameList.addAll(Arrays.asList(interfaceNames));

            // 记录类实现的接口，及类中可能涉及实现的相关方法
            List<MethodAndArgs> implClassMethodWithArgsList = JavaCGByteCodeUtil.genImplClassMethodWithArgs(methods);
            DataContext.CLASS_IMPLEMENTS_METHOD_INFO_MAP.put(className, new ClassImplementsMethodInfo(interfaceNameList, implClassMethodWithArgsList));

            if (!javaClass.isAbstract()) {
                if (interfaceNameList.contains(JavaCGCommonNameConstants.CLASS_NAME_RUNNABLE)) {
                    // 找到Runnable实现类
                    DataContext.RUNNABLE_IMPL_CLASS_MAP.put(className, Boolean.FALSE);
                }
                if (interfaceNameList.contains(JavaCGCommonNameConstants.CLASS_NAME_CALLABLE)) {
                    // 找到Callable实现类
                    DataContext.CALLABLE_IMPL_CLASS_MAP.put(className, Boolean.FALSE);
                }
                if (interfaceNameList.contains(JavaCGCommonNameConstants.CLASS_NAME_TRANSACTION_CALLBACK)) {
                    // 找到TransactionCallback实现类
                    DataContext.TRANSACTION_CALLBACK_IMPL_CLASS_MAP.put(className, Boolean.FALSE);
                }
            }
        }

        // 获得父类和子类信息
        String superClassName = javaClass.getSuperclassName();
        if (JavaCGCommonNameConstants.CLASS_NAME_THREAD.equals(superClassName)) {
            // 找到Thread的子类
            DataContext.THREAD_CHILD_CLASS_MAP.put(className, Boolean.FALSE);
        } else if (JavaCGCommonNameConstants.CLASS_NAME_TIMER_TASK.equals(superClassName)) {
            // 找到TimerTask的子类，按照Runnable实现类处理
            DataContext.RUNNABLE_IMPL_CLASS_MAP.put(className, Boolean.FALSE);
        } else if (JavaCGCommonNameConstants.CLASS_NAME_TRANSACTION_CALLBACK_WITHOUT_RESULT.equals(superClassName)) {
            // 找到TransactionCallbackWithoutResult实现类
            DataContext.TRANSACTION_CALLBACK_WITHOUT_RESULT_CHILD_CLASS_MAP.put(className, Boolean.FALSE);
        }

        if (!JavaCGUtil.isClassInJdk(superClassName)) {
            DataContext.CLASS_EXTENDS_SET.add(className);
            DataContext.CLASS_EXTENDS_SET.add(superClassName);
        }
    }
}
