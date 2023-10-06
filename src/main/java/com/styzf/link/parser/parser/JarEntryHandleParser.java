package com.styzf.link.parser.parser;

import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.common.enums.JavaCGYesNoEnum;
import com.styzf.link.parser.dto.classes.InnerClassInfo;
import com.styzf.link.parser.extensions.manager.ExtensionsManager;
import com.styzf.link.parser.handler.ClassHandler;
import com.styzf.link.parser.spring.UseSpringBeanByAnnotationHandler;
import com.styzf.link.parser.util.JavaCGByteCodeUtil;
import com.styzf.link.parser.util.JavaCGFileUtil;
import com.styzf.link.parser.util.JavaCGLogUtil;
import com.styzf.link.parser.util.JavaCGUtil;
import com.styzf.link.parser.writer.WriterSupportSkip;
import copy.javassist.bytecode.BadBytecode;
import copy.javassist.bytecode.SignatureAttribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Signature;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarInputStream;

import static com.styzf.link.parser.context.CounterContext.CLASS_NUM_COUNTER;
import static com.styzf.link.parser.context.OldDataContext.DUPLICATE_CLASS_NAME_MAP;
import static com.styzf.link.parser.context.OldDataContext.HANDLED_CLASS_NAME_MAP;
import static com.styzf.link.parser.context.OldDataContext.javaCGConfInfo;

/**
 * @author adrninistrator
 * @date 2022/9/14
 * @description: 解析jar包中的文件，正式处理
 */
public class JarEntryHandleParser extends AbstractJarEntryParser {

    private UseSpringBeanByAnnotationHandler useSpringBeanByAnnotationHandler;

    private Writer jarInfoWriter;
    private Writer classNameWriter;
    private Writer methodCallWriter;
    private Writer lambdaMethodInfoWriter;
    private Writer classAnnotationWriter;
    private Writer methodAnnotationWriter;
    private Writer methodLineNumberWriter;
    private Writer methodCallInfoWriter;
    private Writer classInfoWriter;
    private Writer methodInfoWriter;
    private Writer extendsImplWriter;
    private Writer classSignatureEI1Writer;
    private Writer methodArgGenericsTypeWriter;
    private Writer methodReturnGenericsTypeWriter;
    private Writer innerClassWriter;

    private WriterSupportSkip logMethodSpendTimeWriter;

    // 扩展类管理类
    private ExtensionsManager extensionsManager;

    // 已经记录过的jar序号
    private final Set<Integer> recordedJarNum = new HashSet<>();

    @Override
    protected boolean handleEntry(JarInputStream jarInputStream, String jarEntryName) throws IOException {
        // 尝试记录Jar包信息
        tryRecordJarInfo();

        if (!JavaCGFileUtil.isClassFile(jarEntryName)) {
            // 非class文件则跳过
            return true;
        }

        JavaClass javaClass = new ClassParser(jarInputStream, jarEntryName).parse();
        // 处理jar包中的class文件
        return handleClassEntry(javaClass, jarEntryName);
    }
    
    /**
     * 解析java类
     * @param javaClass
     * @param jarEntryName
     * @return
     */
    @Override
    protected boolean handleClassEntry(JavaClass javaClass, String jarEntryName) throws IOException {
        // 处理Java类
        String className = javaClass.getClassName();
        
        if (JavaCGUtil.checkSkipClass(className, javaCGConfInfo.getNeedHandlePackageSet())) {
            if (JavaCGLogUtil.isDebugPrintFlag()) {
                JavaCGLogUtil.debugPrint("跳过不需要处理的类: " + className);
            }
            return true;
        }
        
        List<String> classFilePathList = HANDLED_CLASS_NAME_MAP.get(className);
        if (classFilePathList != null) {
            // 记录已处理过的类名0
            classFilePathList.add(jarEntryName);
            // 记录重复的类名
            DUPLICATE_CLASS_NAME_MAP.put(className, classFilePathList);
            if (JavaCGLogUtil.isDebugPrintFlag()) {
                JavaCGLogUtil.debugPrint("跳过处理重复同名Class: " + className);
            }
            return true;
        }
        
        classFilePathList = new LinkedList<>();
        classFilePathList.add(jarEntryName);
        HANDLED_CLASS_NAME_MAP.put(className, classFilePathList);
        if (JavaCGLogUtil.isDebugPrintFlag()) {
            JavaCGLogUtil.debugPrint("处理Class: " + className);
        }
        
        ClassHandler classHandler = new ClassHandler(javaClass, jarEntryName, javaCGConfInfo);
        classHandler.setUseSpringBeanByAnnotationHandler(useSpringBeanByAnnotationHandler);
        classHandler.setExtensionsManager(extensionsManager);
        classHandler.setLastJarNum(lastJarInfo.getJarNum());
        
        CLASS_NUM_COUNTER.addAndGet();
        if (!classHandler.handleClass()) {
            return false;
        }
        
        // 记录类的信息
        JavaCGFileUtil.write2FileWithTab(classInfoWriter, className, String.valueOf(javaClass.getAccessFlags()));
        recordExtendsAndImplInfo(javaClass, className);
        handleClassSignature(javaClass, className);
        handleInnerClass(javaClass);
        return true;
    }

    // 尝试记录Jar包信息
    private void tryRecordJarInfo() throws IOException {
        int lastJarNum = lastJarInfo.getJarNum();
        if (recordedJarNum.add(lastJarNum)) {
            /*
                当前jar包未记录时
                向文件写入数据，内容为jar包信息
             */
            JavaCGFileUtil.write2FileWithTab(jarInfoWriter, lastJarInfo.getJarType(), String.valueOf(lastJarNum), lastJarInfo.getJarPath());
        }
    }
    
    /**
     * 记录继承及实现相关信息
     * @param javaClass 类信息
     * @param className 类名
     */
    private void recordExtendsAndImplInfo(JavaClass javaClass, String className) {
        String superClassName = javaClass.getSuperclassName();
        String accessFlagsStr = String.valueOf(javaClass.getAccessFlags());
        if (!JavaCGUtil.isObjectClass(superClassName)) {
            // 仅处理父类非Object类的情况
            JavaCGFileUtil.write2FileWithTab(extendsImplWriter,
                    className,
                    accessFlagsStr,
                    JavaCGConstants.FILE_KEY_EXTENDS,
                    superClassName);
        }

        for (String interfaceName : javaClass.getInterfaceNames()) {
            // 接口不会是Object类，不需要判断
            JavaCGFileUtil.write2FileWithTab(extendsImplWriter,
                    className,
                    accessFlagsStr,
                    JavaCGConstants.FILE_KEY_IMPLEMENTS,
                    interfaceName);
        }
    }
    
    /**
     * 处理类的签名
     * @param javaClass 类信息
     * @param className 类名
     */
    private void handleClassSignature(JavaClass javaClass, String className) {
        if (javaClass.isAnnotation()) {
            // 若当前类为注解则不处理
            return;
        }

        Signature signature = JavaCGByteCodeUtil.getSignatureOfClass(javaClass);
        if (signature == null) {
            return;
        }

        try {
            SignatureAttribute.ClassSignature signatureAttribute = SignatureAttribute.toClassSignature(signature.getSignature());
            // 处理父类相关的签名
            SignatureAttribute.ClassType superClassType = signatureAttribute.getSuperClass();
            if (superClassType != null && !JavaCGUtil.isClassInJdk(superClassType.getName())) {
                // 记录类签名中的参数信息
                recordSignatureArgumentInfo(className, JavaCGConstants.FILE_KEY_EXTENDS, superClassType.getName(), superClassType);
            }

            // 处理接口相关的签名
            SignatureAttribute.ClassType[] interfaceClassTypes = signatureAttribute.getInterfaces();
            if (interfaceClassTypes != null) {
                for (SignatureAttribute.ClassType interfaceClassType : interfaceClassTypes) {
                    if (!JavaCGUtil.isClassInJdk(interfaceClassType.getName())) {
                        // 记录类签名中的参数信息
                        recordSignatureArgumentInfo(className, JavaCGConstants.FILE_KEY_IMPLEMENTS, interfaceClassType.getName(), interfaceClassType);
                    }
                }
            }
        } catch (BadBytecode e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 处理内部类信息
     * @param javaClass 类信息
     */
    private void handleInnerClass(JavaClass javaClass) {
        // 获取类中的内部类信息
        List<InnerClassInfo> innerClassInfoList = JavaCGByteCodeUtil.getInnerClassInfo(javaClass);
        for (InnerClassInfo innerClassInfo : innerClassInfoList) {
            JavaCGFileUtil.write2FileWithTab(innerClassWriter, innerClassInfo.getInnerClassName(), innerClassInfo.getOuterClassName(),
                    JavaCGYesNoEnum.parseStrValue(innerClassInfo.isAnonymousClass()));
        }
    }

    /**
     * 记录类签名中的参数信息
     *
     * @param className
     * @param type
     * @param superOrInterfaceName
     * @param classType
     */
    private void recordSignatureArgumentInfo(String className, String type, String superOrInterfaceName, SignatureAttribute.ClassType classType) {
        if (classType.getTypeArguments() == null) {
            return;
        }

        int seq = 0;
        for (SignatureAttribute.TypeArgument typeArgument : classType.getTypeArguments()) {
            SignatureAttribute.ObjectType objectType = typeArgument.getType();
            if (objectType instanceof SignatureAttribute.ClassType) {
                SignatureAttribute.ClassType argumentClassType = (SignatureAttribute.ClassType) objectType;
                JavaCGFileUtil.write2FileWithTab(classSignatureEI1Writer, className, type, superOrInterfaceName, String.valueOf(seq), argumentClassType.getName());
                seq++;
            }
        }
    }

    public void setUseSpringBeanByAnnotationHandler(UseSpringBeanByAnnotationHandler useSpringBeanByAnnotationHandler) {
        this.useSpringBeanByAnnotationHandler = useSpringBeanByAnnotationHandler;
    }

    public void setExtensionsManager(ExtensionsManager extensionsManager) {
        this.extensionsManager = extensionsManager;
    }

}
