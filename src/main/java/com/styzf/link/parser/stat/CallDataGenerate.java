package com.styzf.link.parser.stat;

import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.common.JavaCGConstants;
import com.styzf.link.parser.common.enums.JavaCGCallTypeEnum;
import com.styzf.link.parser.conf.JavaCGConfManager;
import com.styzf.link.parser.conf.JavaCGConfigureWrapper;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.output.JavaCGOutputInfo;
import com.styzf.link.parser.exceptions.JavaCGRuntimeException;
import com.styzf.link.parser.extensions.manager.ExtensionsManager;
import com.styzf.link.parser.generator.puml.PumlXmindGenerator;
import com.styzf.link.parser.generator.txt.MethodCallTxtGeneratot;
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
import com.styzf.link.parser.util.MavenUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.styzf.link.parser.context.CounterContext.CALL_ID_COUNTER;
import static com.styzf.link.parser.context.CounterContext.CLASS_NUM_COUNTER;
import static com.styzf.link.parser.context.CounterContext.METHOD_NUM_COUNTER;
import static com.styzf.link.parser.context.DataContext.javaCGConfInfo;

/**
 * 调用数据生成器
 * @author styzf
 * @date 2023/9/3 21:35
 */
public class CallDataGenerate {

    private final ExtensionsManager extensionsManager = new ExtensionsManager();
    
    private JarEntryPreHandle1Parser jarEntryPreHandle1Parser;
    private JarEntryPreHandle2Parser jarEntryPreHandle2Parser;
    private JarEntryHandleParser jarEntryHandleParser;
    
    private ExtendsImplHandler extendsImplHandler;
    
    private DefineSpringBeanByAnnotationHandler defineSpringBeanByAnnotationHandler;
    
    private UseSpringBeanByAnnotationHandler useSpringBeanByAnnotationHandler;
    
    public static void main(String[] args) {
        new CallDataGenerate().run();
    }
    
    public void run() {
        JavaCGConfigureWrapper configureWrapper = new JavaCGConfigureWrapper(false);
        
        long startTime = System.currentTimeMillis();
        
        javaCGConfInfo = JavaCGConfManager.getConfInfo(configureWrapper);
        
        JavaCGLogUtil.setDebugPrintFlag(javaCGConfInfo.isDebugPrint());
        JavaCGLogUtil.setDebugPrintInFile(javaCGConfInfo.isDebugPrintInFile());
        
        // 处理参数中指定的jar包
        File newJarFile = handleJarInConf();
        
        String newJarFilePath = JavaCGFileUtil.getCanonicalPath(newJarFile);
        String outputRootPath = javaCGConfInfo.getOutputRootPath();
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
            return;
        }
        
        // 初始化
        if (!init(outputDirPath)) {
            return;
        }
        
        // 处理jar包，主逻辑
        if (!handleJar(newJarFilePath)) {
            return;
        }
    
        long spendTime = System.currentTimeMillis() - startTime;
        String printInfo = "执行解析完毕，处理数量，类： " + CLASS_NUM_COUNTER.getCount() +
                " ，方法: " + METHOD_NUM_COUNTER.getCount() +
                " ，方法调用: " + CALL_ID_COUNTER.getCount() +
                " ，耗时: " + (spendTime / 1000.0D) + " S";
        System.out.println(printInfo);
        
        new MethodCallTxtGeneratot().generateCalcTime();
        new PumlXmindGenerator().generateCalcTime();
//        new BaseXmindGenerator().generateCalcTime();
        
        spendTime = System.currentTimeMillis() - startTime;
        printInfo = "执行完毕，处理数量，类： " + CLASS_NUM_COUNTER.getCount() +
                " ，方法: " + METHOD_NUM_COUNTER.getCount() +
                " ，方法调用: " + CALL_ID_COUNTER.getCount() +
                " ，耗时: " + (spendTime / 1000.0D) + " S";
        System.out.println(printInfo);
        if (JavaCGLogUtil.isDebugPrintInFile()) {
            JavaCGLogUtil.debugPrint(printInfo);
        }
    }
    
    /**
     * 处理配置参数中指定的jar包
     * @return 处理后的jar包
     */
    private File handleJarInConf() {
        List<String> jarList = new ArrayList<>();
        List<String> jarDirList = javaCGConfInfo.getJarDirList();
        if (jarDirList.isEmpty()) {
            throw new JavaCGRuntimeException("请在配置文件" + JavaCGConstants.FILE_CONFIG + "中指定需要处理的jar包或目录列表");
        }
        
        System.out.println("需要处理的jar包或目录:");
        for (String jarDir : jarDirList) {
            if (jarDir.endsWith("pom.xml")) {
                File pomFile = new File(jarDir);
                if (!pomFile.exists()) {
                    continue;
                }
                
                List<String> depList = MavenUtils.getDepList(pomFile, javaCGConfInfo.getMavenHome());
                jarList.addAll(depList);
                depList.forEach(System.out::println);
            } else {
                jarList.add(jarDir);
                System.out.println(jarDir);
            }
        }
        
        DataContext.JAR_INFO_MAP = new HashMap<>(jarList.size());
        
        Set<String> needHandlePackageSet = javaCGConfInfo.getNeedHandlePackageSet();
        // 对指定的jar包进行处理
        File newJarFile = JavaCGJarUtil.handleJar(jarList, needHandlePackageSet);
        if (newJarFile == null) {
            throw new JavaCGRuntimeException("jar处理失败");
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
        // java-callgraph2处理结果信息
        JavaCGOutputInfo javaCGOutputInfo = new JavaCGOutputInfo(dirPath, javaCGConfInfo.getOutputFileExt());
        
        // 扩展类管理类初始化
        extensionsManager.setJavaCGOutputInfo(javaCGOutputInfo);
        if (!extensionsManager.init()) {
            return false;
        }
        
        if (javaCGConfInfo.isParseMethodCallTypeValue()) {
            defineSpringBeanByAnnotationHandler = new DefineSpringBeanByAnnotationHandler(javaCGConfInfo);
        }
        jarEntryPreHandle1Parser = new JarEntryPreHandle1Parser(defineSpringBeanByAnnotationHandler, extensionsManager);
        
        if (javaCGConfInfo.isParseMethodCallTypeValue()) {
            useSpringBeanByAnnotationHandler = new UseSpringBeanByAnnotationHandler(
                    defineSpringBeanByAnnotationHandler,
                    extensionsManager.getSpringXmlBeanParser());
        }
        jarEntryPreHandle2Parser = new JarEntryPreHandle2Parser(useSpringBeanByAnnotationHandler);
        
        // 正式处理相关
        jarEntryHandleParser = new JarEntryHandleParser();
        jarEntryHandleParser.setUseSpringBeanByAnnotationHandler(useSpringBeanByAnnotationHandler);
        jarEntryHandleParser.setExtensionsManager(extensionsManager);
        
        // 继承及实现相关的方法处理相关
        extendsImplHandler = new ExtendsImplHandler();
        extendsImplHandler.setJavaCGConfInfo(javaCGConfInfo);
        return true;
    }
    
    // 处理一个jar包
    private boolean handleJar(String jarFilePath) {
        try {
            // 对Class进行预处理
            if (!jarEntryPreHandle1Parser.parse(jarFilePath)) {
                return false;
            }
            
            // 对Class进行第二次预处理
            if (!jarEntryPreHandle2Parser.parse(jarFilePath)) {
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
        if (DataContext.DUPLICATE_CLASS_NAME_MAP.isEmpty()) {
            JavaCGLogUtil.debugPrint("不存在重复的类名");
            return;
        }
        
        List<String> duplicateClassNameList = new ArrayList<>(DataContext.DUPLICATE_CLASS_NAME_MAP.keySet());
        Collections.sort(duplicateClassNameList);
        
        for (String duplicateClassName : duplicateClassNameList) {
            List<String> classFilePathList = DataContext.DUPLICATE_CLASS_NAME_MAP.get(duplicateClassName);
            JavaCGLogUtil.debugPrint("重复的类名 " + duplicateClassName + " 使用的class文件 " + classFilePathList.get(0));
            for (int i = 1; i < classFilePathList.size(); i++) {
                JavaCGLogUtil.debugPrint("重复的类名 " + duplicateClassName + " 跳过的class文件 " + classFilePathList.get(i));
            }
        }
    }
}
