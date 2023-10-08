package com.styzf.link.parser.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.data.ClassExtendsMethodInfo;
import com.styzf.link.parser.data.ClassImplementsMethodInfo;
import com.styzf.link.parser.dto.interfaces.InterfaceExtendsMethodInfo;
import com.styzf.link.parser.exceptions.JavaCGRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.styzf.link.parser.common.JavaCGCommonNameConstants.CLASS_NAME_OBJECT;
import static com.styzf.link.parser.common.JavaCGCommonNameConstants.METHOD_NAME_INIT;
import static com.styzf.link.parser.context.DataContext.CLASS_IMPL_METHOD_INFO_MAP;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author adrninistrator
 * @date 2021/6/22
 * @description:
 */

public class JavaCGUtil {

    /**
     * 判断类名是否为匿名内部类
     *
     * @param className
     * @return
     */
    public static boolean isInnerAnonymousClass(String className) {
        String tail = StrUtil.subAfter(className, "$", true);
        return isNumStr(tail);
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumStr(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }

        char[] charArray = str.toCharArray();
        for (char ch : charArray) {
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * 将不可重复读的InputStream缓存为可以重复读取的ByteArrayInputStream
     *
     * @param inputStream
     * @return
     */
    public static InputStream cacheInputStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int size;
            while ((size = inputStream.read(data)) != -1) {
                baos.write(data, 0, size);
            }

            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            System.err.println("出现异常 " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断是否为Object类
     *
     * @param className
     * @return
     */
    public static boolean isObjectClass(String className) {
        return CLASS_NAME_OBJECT.equals(className);
    }

    /**
     * 判断指定类是否为JDK中的类
     *
     * @param className
     * @return
     */
    public static boolean isClassInJdk(String className) {
        return StrUtil.startWith(className, "java.");
    }

    /**
     * 判断是否为<init>方法
     *
     * @param methodName
     * @return
     */
    public static boolean isInitMethod(String methodName) {
        return METHOD_NAME_INIT.equals(methodName);
    }

    /**
     * 判断字符串是否以指定的字符串数组结尾，忽略大小写
     *
     * @param data
     * @param array
     * @return
     */
    public static boolean isStringEndWithArrayIgnoreCase(String data, String[] array) {
        if (data == null || array == null || array.length == 0) {
            return false;
        }

        for (String arrayStr : array) {
            if (StrUtil.endWithIgnoreCase(data, arrayStr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从完整类名中获取简单类名（去掉包名）
     *
     * @param className 完整类名
     * @return
     */
    public static String getSimpleClassNameFromFull(String className) {
        int indexLastDot = className.lastIndexOf(JavaCGConstants.FLAG_DOT);
        if (indexLastDot == -1) {
            return className;
        }
        return className.substring(indexLastDot + 1);
    }

    /**
     * 获取简单类名首字母小写后的结果
     *
     * @param simpleClassName 简单类名
     * @return
     */
    public static String getFirstLetterLowerClassName(String simpleClassName) {
        if (simpleClassName == null) {
            return null;
        }

        if (simpleClassName.isEmpty()) {
            return "";
        }

        String firstLetterLower = simpleClassName.substring(0, 1).toLowerCase();
        if (simpleClassName.length() == 1) {
            return firstLetterLower;
        }

        return firstLetterLower + simpleClassName.substring(1);
    }

    /**
     * 获取类的包名
     *
     * @param className
     * @return
     */
    public static String getPackageName(String className) {
        return StrUtil.subBefore(className, JavaCGConstants.FLAG_DOT, true);
    }

    /**
     * 判断指定的两个类的包名是否相同
     *
     * @param className1
     * @param className2
     * @return
     */
    public static boolean checkSamePackage(String className1, String className2) {
        return StrUtil.equals(getPackageName(className1), getPackageName(className2));
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String currentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        return sdf.format(new Date());
    }

    /**
     * 判断是否需要跳过当前类的处理
     *
     * @param className
     * @param needHandlePackageSet
     * @return false: 不跳过 true: 跳过
     */
    public static boolean checkSkipClass(String className, Set<String> needHandlePackageSet) {
        if (CollUtil.isEmpty(needHandlePackageSet)) {
            return false;
        }
        for (String needHandlePackage : needHandlePackageSet) {
            if (StrUtil.startWith(className, needHandlePackage)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 为文件路径增加分隔符
     *
     * @param filePath
     * @return
     */
    public static String addSeparator4FilePath(String filePath) {
        if (StrUtil.endWithAny(filePath, "/", "\\")) {
            // 文件路径以分隔符结尾，则直接使用
            return filePath;
        }

        // 文件路径没有以分隔符结尾，则在后面增加分隔符
        return filePath + File.separator;
    }

    /**
     * 获取JVM参数中指定的目录路径
     *
     * @param jvmOptionKey
     * @return
     */
    public static String getDirPathInJvmOptions(String jvmOptionKey) {
        String dirPath = System.getProperty(jvmOptionKey);
        if (dirPath == null) {
            return "";
        }

        return addSeparator4FilePath(dirPath);
    }

    /**
     * base64编码
     *
     * @param data
     * @return
     */
    public static String base64Encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(UTF_8));
    }

    /**
     * base64解码
     *
     * @param data
     * @return
     */
    public static String base64Decode(String data) {
        return new String(Base64.getDecoder().decode(data), UTF_8);
    }

    /**
     * 根据不定长数组生成HashSet
     *
     * @param a
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> Set<T> genSetFromArray(T... a) {
        if (ArrayUtil.isEmpty(a)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(a));
    }

    /**
     * 根据不定长数组生成List
     *
     * @param a
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> List<T> genListFromArray(T... a) {
        if (ArrayUtil.isEmpty(a)) {
            return new ArrayList<>();
        }
        return Arrays.asList(a);
    }


    /**
     * 判断childClassName是否直接或间接继承自superClassName
     *
     * @param childClassName            子类类名
     * @param superClassName            超类类名
     * @param classExtendsMethodInfoMap 类涉及继承的信息
     * @return
     */
    public static boolean isChildOf(String childClassName, String superClassName, Map<String, ClassExtendsMethodInfo> classExtendsMethodInfoMap) {
        if (childClassName == null || superClassName == null || classExtendsMethodInfoMap == null) {
            throw new JavaCGRuntimeException("传入参数不允许为空");
        }

        String currentClassName = childClassName;
        while (true) {
            ClassExtendsMethodInfo classExtendsMethodInfo = classExtendsMethodInfoMap.get(currentClassName);
            if (classExtendsMethodInfo == null) {
                // 找不到当前类的父类信息
                return false;
            }

            if (superClassName.equals(classExtendsMethodInfo.getSuperClassName())) {
                // 当前类的父类是指定的父类
                return true;
            }

            // 继续处理父类
            currentClassName = classExtendsMethodInfo.getSuperClassName();
        }
    }

    /**
     * 判断childClassName是否直接或间接实现了interfaceName
     *
     * @param className                     类名
     * @param interfaceName                 接口名
     * @param classExtendsMethodInfoMap     类涉及继承的信息
     * @param interfaceExtendsMethodInfoMap 接口涉及继承的信息
     * @return
     */
    public static boolean isImplementationOf(String className,
                                             String interfaceName,
                                             Map<String, ClassExtendsMethodInfo> classExtendsMethodInfoMap,
                                             Map<String, InterfaceExtendsMethodInfo> interfaceExtendsMethodInfoMap
    ) {
        if (className == null || interfaceName == null || classExtendsMethodInfoMap == null || interfaceExtendsMethodInfoMap == null) {
            throw new JavaCGRuntimeException("传入参数不允许为空");
        }

        String currentClassName = className;
        while (true) {
            ClassImplementsMethodInfo classImplementsMethodInfo = CLASS_IMPL_METHOD_INFO_MAP.get(currentClassName);
            if (classImplementsMethodInfo != null) {
                List<String> interfaceNameList = classImplementsMethodInfo.getInterfaceNameList();
                if (interfaceNameList != null) {
                    if (interfaceNameList.contains(interfaceName)) {
                        // 当前类实现的接口中包含指定的接口
                        return true;
                    }

                    for (String currentInterfaceName : interfaceNameList) {
                        if (isSuperInterfaceOf(currentInterfaceName, interfaceName, interfaceExtendsMethodInfoMap)) {
                            // 当前类实现的接口继承了指定的接口
                            return true;
                        }
                    }
                }
            }

            ClassExtendsMethodInfo classExtendsMethodInfo = classExtendsMethodInfoMap.get(currentClassName);
            if (classExtendsMethodInfo == null) {
                // 找不到当前类实现的接口信息
                return false;
            }

            // 继续处理父类
            currentClassName = classExtendsMethodInfo.getSuperClassName();
        }
    }

    /**
     * @param childInterfaceName            子类接口名
     * @param superInterfaceName            超类接口名
     * @param interfaceExtendsMethodInfoMap 接口涉及继承的信息
     * @return
     */
    public static boolean isSuperInterfaceOf(String childInterfaceName, String superInterfaceName, Map<String, InterfaceExtendsMethodInfo> interfaceExtendsMethodInfoMap) {
        if (childInterfaceName == null || superInterfaceName == null || interfaceExtendsMethodInfoMap == null) {
            throw new JavaCGRuntimeException("传入参数不允许为空");
        }

        while (true) {
            InterfaceExtendsMethodInfo interfaceExtendsMethodInfo = interfaceExtendsMethodInfoMap.get(childInterfaceName);
            if (interfaceExtendsMethodInfo == null) {
                // 找不到当前接口继承的接口信息
                return false;
            }

            List<String> superInterfaceList = interfaceExtendsMethodInfo.getSuperInterfaceList();
            if (superInterfaceList.isEmpty()) {
                // 找不到当前接口继承的接口信息
                return false;
            }

            if (superInterfaceList.contains(superInterfaceName)) {
                // 当前接口继承的接口包含指定接口
                return true;
            }

            // 处理当前接口继承的接口，递归调用
            for (String currentSuperInterfaceName : superInterfaceList) {
                if (isSuperInterfaceOf(currentSuperInterfaceName, superInterfaceName, interfaceExtendsMethodInfoMap)) {
                    return true;
                }
            }
            // 当前接口继承的接口也没有继承指定的超类接口
            return false;
        }
    }

    private JavaCGUtil() {
        throw new IllegalStateException("illegal");
    }
}
