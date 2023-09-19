package com.styzf.link.parser.stat;

import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.common.enums.JavaCGCallTypeEnum;
import com.styzf.link.parser.conf.JavaCGConfManager;
import com.styzf.link.parser.conf.JavaCGConfigureWrapper;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.counter.JavaCGCounter;
import com.styzf.link.parser.dto.output.JavaCGOutputInfo;
import com.styzf.link.parser.exceptions.JavaCGRuntimeException;
import com.styzf.link.parser.extensions.annotation_attributes.AnnotationAttributesFormatterInterface;
import com.styzf.link.parser.extensions.code_parser.CodeParserInterface;
import com.styzf.link.parser.extensions.code_parser.SpringXmlBeanParserInterface;
import com.styzf.link.parser.extensions.manager.ExtensionsManager;
import com.styzf.link.parser.generator.puml.PumlXmindGenerator;
import com.styzf.link.parser.generator.txt.MethodCallTxtGeneratot;
import com.styzf.link.parser.generator.xmind.BaseXmindGenerator;
import com.styzf.link.parser.handler.ExtendsImplHandler;
import com.styzf.link.parser.parser.JarEntryHandleParser;
import com.styzf.link.parser.parser.JarEntryPreHandle1Parser;
import com.styzf.link.parser.parser.JarEntryPreHandle2Parser;
import com.styzf.link.parser.spring.DefineSpringBeanByAnnotationHandler;
import com.styzf.link.parser.spring.UseSpringBeanByAnnotationHandler;
import com.styzf.link.parser.util.JavaCGFileUtil;
import com.styzf.link.parser.util.JavaCGJarUtil;
import com.styzf.link.parser.util.JavaCGLogUtil;
import com.styzf.link.parser.util.JavaCGUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 调用数据生成器
 * @author styzf
 * @date 2023/9/3 21:35
 */
public class CallDataGenerate {
    private final JavaCGCounter callIdCounter = new JavaCGCounter(JavaCGConstants.METHOD_CALL_ID_START);
    private final JavaCGCounter classNumCounter = new JavaCGCounter(0);
    private final JavaCGCounter methodNumCounter = new JavaCGCounter(0);
    private final ExtensionsManager extensionsManager = new ExtensionsManager();
    
    private JarEntryPreHandle1Parser jarEntryPreHandle1Parser;
    private JarEntryPreHandle2Parser jarEntryPreHandle2Parser;
    private JarEntryHandleParser jarEntryHandleParser;
    
    private ExtendsImplHandler extendsImplHandler;
    
    // java-callgraph2处理结果信息
    private JavaCGOutputInfo javaCGOutputInfo;
    
    private DefineSpringBeanByAnnotationHandler defineSpringBeanByAnnotationHandler;
    
    private UseSpringBeanByAnnotationHandler useSpringBeanByAnnotationHandler;
    
    private final Map<String, List<String>> duplicateClassNameMap = DataContext.DUPLICATE_CLASS_NAME_MAP;;
    
    private DataContext dataContext = new DataContext();
    
    public static void main(String[] args) {
        new CallDataGenerate().run(new JavaCGConfigureWrapper(false));
    }
    
    public boolean run(JavaCGConfigureWrapper javaCGConfigureWrapper) {
        if (javaCGConfigureWrapper == null) {
            throw new JavaCGRuntimeException("配置参数包装对象不允许为null");
        }
        
        long startTime = System.currentTimeMillis();
        
        DataContext.javaCGConfInfo = JavaCGConfManager.getConfInfo(javaCGConfigureWrapper);
        
        JavaCGLogUtil.setDebugPrintFlag(DataContext.javaCGConfInfo.isDebugPrint());
        JavaCGLogUtil.setDebugPrintInFile(DataContext.javaCGConfInfo.isDebugPrintInFile());
        
        // 处理参数中指定的jar包
        File newJarFile = handleJarInConf();
        if (newJarFile == null) {
            return false;
        }
        
        String newJarFilePath = JavaCGFileUtil.getCanonicalPath(newJarFile);
        String outputRootPath = DataContext.javaCGConfInfo.getOutputRootPath();
        String outputDirPath;
        if (StrUtil.isBlank(outputRootPath)) {
            // 配置参数中未指定生成文件的根目录，生成在jar包所在目录
            outputDirPath = newJarFilePath + JavaCGConstants.DIR_TAIL_OUTPUT + File.separator;
        } else {
            // 配置参数中有指定生成文件的根目录，生成在指定目录
            outputDirPath = JavaCGUtil.addSeparator4FilePath(outputRootPath) + newJarFile.getName() + JavaCGConstants.DIR_TAIL_OUTPUT + File.separator;
        }
        System.out.println("当前输出的根目录: " + outputDirPath);
        if (!JavaCGFileUtil.isDirectoryExists(outputDirPath, true)) {
            return false;
        }
        
        // 初始化
        if (!init(outputDirPath)) {
            return false;
        }
        
        // 处理jar包，主逻辑
        if (!handleJar(newJarFilePath)) {
            return false;
        }
    
        long spendTime = System.currentTimeMillis() - startTime;
        String printInfo = "执行解析完毕，处理数量，类： " + classNumCounter.getCount() +
                " ，方法: " + methodNumCounter.getCount() +
                " ，方法调用: " + callIdCounter.getCount() +
                " ，耗时: " + (spendTime / 1000.0D) + " S";
        System.out.println(printInfo);
        
        new MethodCallTxtGeneratot().generateCalcTime();
        new PumlXmindGenerator().generateCalcTime();
        new BaseXmindGenerator().generateCalcTime();
        
        spendTime = System.currentTimeMillis() - startTime;
        printInfo = "执行完毕，处理数量，类： " + classNumCounter.getCount() +
                " ，方法: " + methodNumCounter.getCount() +
                " ，方法调用: " + callIdCounter.getCount() +
                " ，耗时: " + (spendTime / 1000.0D) + " S";
        System.out.println(printInfo);
        if (JavaCGLogUtil.isDebugPrintInFile()) {
            JavaCGLogUtil.debugPrint(printInfo);
        }
        return true;
    }
    
    // 处理配置参数中指定的jar包
    private File handleJarInConf() {
        List<String> jarDirList = DataContext.javaCGConfInfo.getJarDirList();
        if (jarDirList.isEmpty()) {
            System.err.println("请在配置文件" + JavaCGConstants.FILE_CONFIG + "中指定需要处理的jar包或目录列表");
            return null;
        }
        
        System.out.println("需要处理的jar包或目录:");
        for (String jarDir : jarDirList) {
            System.out.println(jarDir);
        }
        
        DataContext.JAR_INFO_MAP = new HashMap<>(jarDirList.size());
        
        Set<String> needHandlePackageSet = DataContext.javaCGConfInfo.getNeedHandlePackageSet();
        // 对指定的jar包进行处理
        File newJarFile = JavaCGJarUtil.handleJar(jarDirList, needHandlePackageSet);
        if (newJarFile == null) {
            return null;
        }
        
        System.out.println("实际处理的jar文件: " + JavaCGFileUtil.getCanonicalPath(newJarFile));
        
        if (needHandlePackageSet.isEmpty()) {
            System.out.println("所有包中的class文件都需要处理");
        } else {
            List<String> needHandlePackageList = new ArrayList<>(needHandlePackageSet);
            Collections.sort(needHandlePackageList);
            System.out.println("仅处理以下包中的class文件\n" + StrUtil.join("\n", needHandlePackageList.toArray()));
        }
        return newJarFile;
    }
    
    private boolean init(String dirPath) {
        // 检查方法调用枚举类型是否重复定义
        JavaCGCallTypeEnum.checkRepeat();
        
        // 处理结果信息相关
        javaCGOutputInfo = new JavaCGOutputInfo(dirPath, DataContext.javaCGConfInfo.getOutputFileExt());
        
        // 扩展类管理类初始化
        extensionsManager.setJavaCGOutputInfo(javaCGOutputInfo);
        if (!extensionsManager.init()) {
            return false;
        }
        
        if (DataContext.javaCGConfInfo.isParseMethodCallTypeValue()) {
            defineSpringBeanByAnnotationHandler = new DefineSpringBeanByAnnotationHandler(DataContext.javaCGConfInfo);
        }
        jarEntryPreHandle1Parser = new JarEntryPreHandle1Parser(defineSpringBeanByAnnotationHandler, extensionsManager);
        
        if (DataContext.javaCGConfInfo.isParseMethodCallTypeValue()) {
            useSpringBeanByAnnotationHandler = new UseSpringBeanByAnnotationHandler(
                    defineSpringBeanByAnnotationHandler,
                    extensionsManager.getSpringXmlBeanParser());
        }
        jarEntryPreHandle2Parser = new JarEntryPreHandle2Parser(useSpringBeanByAnnotationHandler);
        
        // 正式处理相关
        jarEntryHandleParser = new JarEntryHandleParser();
        jarEntryHandleParser.setUseSpringBeanByAnnotationHandler(useSpringBeanByAnnotationHandler);
        jarEntryHandleParser.setExtensionsManager(extensionsManager);
        jarEntryHandleParser.setCallIdCounter(callIdCounter);
        jarEntryHandleParser.setClassNumCounter(classNumCounter);
        jarEntryHandleParser.setMethodNumCounter(methodNumCounter);
        
        // 继承及实现相关的方法处理相关
        extendsImplHandler = new ExtendsImplHandler();
        extendsImplHandler.setJavaCGConfInfo(DataContext.javaCGConfInfo);
        extendsImplHandler.setCallIdCounter(callIdCounter);
        return true;
    }
    
    // 处理一个jar包
    private boolean handleJar(String jarFilePath) {
        try {
            // 对Class进行预处理
            if (!preHandleClasses1(jarFilePath)) {
                return false;
            }
            
            // 对Class进行第二次预处理
            if (!preHandleClasses2(jarFilePath)) {
                return false;
            }
            
            // 处理当前jar包中的class文件
            if (!jarEntryHandleParser.parse(jarFilePath)) {
                return false;
            }
            
            // 打印重复的类名
            printDuplicateClasses();
            
            // 处理继承及实现相关的方法
            extendsImplHandler.handle();
            
            // 记录Spring Bean的名称及类型
            recordSpringBeanNameAndType();
            return true;
        } catch (Exception e) {
            System.err.println("处理jar包出现异常 " + jarFilePath);
            e.printStackTrace();
            return false;
        }
    }
    
    // 对Class进行预处理
    private boolean preHandleClasses1(String jarFilePath) {
        return jarEntryPreHandle1Parser.parse(jarFilePath);
    }
    
    // 对Class进行第二次预处理
    private boolean preHandleClasses2(String jarFilePath) {
        return jarEntryPreHandle2Parser.parse(jarFilePath);
    }
    
    // 记录Spring Bean的名称及类型
    private void recordSpringBeanNameAndType() throws IOException {
        if (defineSpringBeanByAnnotationHandler == null) {
            return;
        }
        
        for (String springBeanName : defineSpringBeanByAnnotationHandler.getSpringBeanNameSet()) {
            List<String> springBeanTypeList = defineSpringBeanByAnnotationHandler.getSpringBeanTypeList(springBeanName);
            for (int i = 0; i < springBeanTypeList.size(); i++) {
                JavaCGFileUtil.write2FileWithTab(null, springBeanName, String.valueOf(i), springBeanTypeList.get(i));
            }
        }
    }
    
    // 打印重复的类名
    private void printDuplicateClasses() {
        if (duplicateClassNameMap.isEmpty()) {
            JavaCGLogUtil.debugPrint("不存在重复的类名");
            return;
        }
        
        List<String> duplicateClassNameList = new ArrayList<>(duplicateClassNameMap.keySet());
        Collections.sort(duplicateClassNameList);
        
        for (String duplicateClassName : duplicateClassNameList) {
            List<String> classFilePathList = duplicateClassNameMap.get(duplicateClassName);
            JavaCGLogUtil.debugPrint("重复的类名 " + duplicateClassName + " 使用的class文件 " + classFilePathList.get(0));
            for (int i = 1; i < classFilePathList.size(); i++) {
                JavaCGLogUtil.debugPrint("重复的类名 " + duplicateClassName + " 跳过的class文件 " + classFilePathList.get(i));
            }
        }
    }
    
    /**
     * 添加自定义代码解析类
     * 需要在调用run()方法之前调用当前方法
     *
     * @param codeParser
     */
    public void addCodeParser(CodeParserInterface codeParser) {
        extensionsManager.addCodeParser(codeParser);
    }
    
    /**
     * 设置对Spring XML中的Bean解析的类
     * 需要在调用run()方法之前调用当前方法
     *
     * @param springXmlBeanParser
     */
    public void setSpringXmlBeanParser(SpringXmlBeanParserInterface springXmlBeanParser) {
        extensionsManager.setSpringXmlBeanParser(springXmlBeanParser);
    }
    
    // 获取java-callgraph2处理结果信息
    public JavaCGOutputInfo getJavaCGOutputInfo() {
        return javaCGOutputInfo;
    }
    
    /**
     * 获取重复类名Map
     *
     * @return
     */
    public Map<String, List<String>> getDuplicateClassNameMap() {
        return duplicateClassNameMap;
    }
    
    /**
     * 设置注解属性格式化类
     *
     * @param annotationAttributesFormatter
     */
    public void setAnnotationAttributesFormatter(AnnotationAttributesFormatterInterface annotationAttributesFormatter) {
        extensionsManager.setAnnotationAttributesFormatter(annotationAttributesFormatter);
    }
}
