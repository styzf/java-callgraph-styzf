package com.styzf.link.parser.context;

import cn.hutool.core.collection.CollUtil;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.conf.JavaCGConfInfo;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.classes.ClassExtendsMethodInfo;
import com.styzf.link.parser.dto.classes.ClassImplementsMethodInfo;
import com.styzf.link.parser.dto.doc.DocDto;
import com.styzf.link.parser.dto.interfaces.InterfaceExtendsMethodInfo;
import com.styzf.link.parser.dto.jar.ClassAndJarNum;
import com.styzf.link.parser.dto.jar.JarInfo;
import com.styzf.link.parser.dto.method.MethodAndArgs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 解析结果上下文对象，存储数据专用
 * @author styzf
 * @date 2023/9/6 20:23
 */
public class DataContext {
    
    /**
     * 配置数据
     */
    public static JavaCGConfInfo javaCGConfInfo;
    
    /**
     * 类名及所在的jar包序号Map
     * key    类名
     * value  类所在的jar包序号
     */
    public final static ClassAndJarNum CLASS_AND_JAR_NUM = new ClassAndJarNum();
    
    /**
     * 类实现的接口，及类中的方法信息
     * key     类名
     * value   类实现的接口，及类中的方法信息
     */
    public final static Map<String, ClassImplementsMethodInfo> CLASS_IMPLEMENTS_METHOD_INFO_MAP = new HashMap<>(JavaCGConstants.SIZE_200);
    
    /**
     * 接口中的方法信息
     * key     接口名
     * value   接口中的方法信息
     */
    public final static Map<String, List<MethodAndArgs>> INTERFACE_METHOD_WITH_ARGS_MAP = new HashMap<>(JavaCGConstants.SIZE_200);
    
    // 第一次预处理相关
    /**
     * Runnable实现类Map
     */
    public final static Map<String, Boolean> RUNNABLE_IMPL_CLASS_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    /**
     * Callable实现类Map
     */
    public final static Map<String, Boolean> CALLABLE_IMPL_CLASS_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    /**
     * TransactionCallback实现类Map
     */
    public final static Map<String, Boolean> TRANSACTION_CALLBACK_IMPL_CLASS_MAP = new HashMap<>(JavaCGConstants.SIZE_10);
    /**
     * TransactionCallbackWithoutResult子类Map
     */
    public final static Map<String, Boolean> TRANSACTION_CALLBACK_WITHOUT_RESULT_CHILD_CLASS_MAP = new HashMap<>(JavaCGConstants.SIZE_10);
    /**
     * Thread子类Map
     */
    public final static Map<String, Boolean> THREAD_CHILD_CLASS_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    /**
     * 涉及继承的类名Set
     */
    public final static Set<String> CLASS_EXTENDS_SET = new HashSet<>(JavaCGConstants.SIZE_100);
    /**
     * 涉及继承的接口名Set
     */
    public final static Set<String> INTERFACE_EXTENDS_SET = new HashSet<>(JavaCGConstants.SIZE_100);
    
    // 第二次预处理相关
    /**
     * 类涉及继承的信息
     * key
     *     类名
     * value
     *     类涉及继承的信息，包含类的accessFlags，父类，及类中的方法信息
     */
    public final static Map<String, ClassExtendsMethodInfo> CLASS_EXTENDS_METHOD_INFO_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    
    /**
     * 父类对应的子类信息
     * key
     *     父类类名
     * value
     *     子类类名列表
     */
    public final static Map<String, List<String>> CHILDREN_CLASS_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    
    /**
     * 接口涉及继承的信息
     * key
     *     类名
     * value
     *     接口继承的信息，包括接口继承的接口，及接口中的方法
     */
    public final static Map<String, InterfaceExtendsMethodInfo> INTERFACE_EXTENDS_METHOD_INFO_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    
    /**
     * 父接口对应的子接口信息
     * key
     *     父接口类名
     * value
     *     子接口类名列表
     */
    public final static Map<String, List<String>> CHILDREN_INTERFACE_MAP = new HashMap<>(JavaCGConstants.SIZE_100);
    
    /**
     * 保存需要处理的jar包文件名及对应的jar包信息
     * key     jar包文件名
     * value   jar包信息，包含jar包序号
     */
    public static Map<String, JarInfo> JAR_INFO_MAP;
    
    /**
     * 记录已处理过的类名
     * key
     *     类名
     * value
     *     class文件路径
     */
    public final static Map<String, List<String>> HANDLED_CLASS_NAME_MAP = new HashMap<>();
    
    // 重复的类名，结构同上
    public final static Map<String, List<String>> DUPLICATE_CLASS_NAME_MAP = new HashMap<>();
    
    /**
     * 方法调用主要数据
     */
    public final static Map<String, List<MethodCall>> METHOD_CALL_MAP = new HashMap<>(2^1024);

    /**
     * 方法被调用主要数据
     */
    public final static Map<String, List<MethodCall>> METHOD_CALLEE_MAP = new HashMap<>(2^1024);
    
    public final static Map<String, DocDto> DOC_MAP = new HashMap<>(2^1024);
    
    public static void putMethodCallMap(MethodCall methodCall) {
        List<MethodCall> methodCallList = DataContext.METHOD_CALL_MAP.get(methodCall.genCallerFullMethod());
        if (methodCallList == null) {
            methodCallList = new ArrayList<>();
        }
        methodCallList.add(methodCall);
    
        List<MethodCall> methodCalleeList = DataContext.METHOD_CALLEE_MAP.get(methodCall.genCalleeFullMethod());
        if (methodCalleeList == null) {
            methodCalleeList = new ArrayList<>();
        }
        methodCalleeList.add(methodCall);
        
        String callerFullMethod = methodCall.genCallerFullMethod();
        String calleeFullMethod = methodCall.genCalleeFullMethod();
        
        DataContext.METHOD_CALL_MAP.put(callerFullMethod, methodCallList);
        DataContext.METHOD_CALLEE_MAP.put(calleeFullMethod, methodCalleeList);
    }
    
    public static String getRootMethodName(String rootMethodName) {
        List<MethodCall> list = DataContext.METHOD_CALL_MAP.get(rootMethodName);
        String methodName;
        if (CollUtil.isEmpty(list)) {
            methodName = DataContext.METHOD_CALL_MAP.keySet().stream()
                    .filter(key -> key.startsWith(rootMethodName))
                    .findFirst()
                    .orElse(rootMethodName);
        } else {
            methodName = rootMethodName;
        }
        return methodName;
    }
}
