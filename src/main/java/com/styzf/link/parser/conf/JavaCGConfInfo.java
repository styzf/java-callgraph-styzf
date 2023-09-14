package com.styzf.link.parser.conf;

import java.util.List;
import java.util.Set;

/**
 * @author adrninistrator
 * @date 2022/11/4
 * @description:
 */
public class JavaCGConfInfo {

    // 需要处理的jar包或目录
    private List<String> jarDirList;
    
    // 需要处理的jar包或目录
    private List<String> sourcesDirList;

    // 需要处理的的包名
    private Set<String> needHandlePackageSet;

    // 处理方法调用时是否解析可能的类型与值
    private boolean parseMethodCallTypeValue;

    // 处理类的方法前是否需要先解析构造函数以非静态字段可能的类型
    private boolean firstParseInitMethodType;

    // 处理方法出现异常时，是否要继续
    private boolean continueWhenError;

    // 调试日志打印开关
    private boolean debugPrint;

    // 调试日志打印到文件开关
    private boolean debugPrintInFile;

    // 记录方法分析耗时的开关
    private boolean logMethodSpendTime;

    // 生成文件的根目录
    private String outputRootPath;

    // 生成文件后缀名
    private String outputFileExt;
    
    /**
     * 要解析得根方法
     */
    private String rootMethodName;
    
    /**
     * 要解析得根方法的方向是否为向下解析
     */
    private boolean rootMethodNext;
    
    public String getRootMethodName() {
        return rootMethodName;
    }
    
    public void setRootMethodName(String rootMethodName) {
        this.rootMethodName = rootMethodName;
    }
    
    public boolean isRootMethodNext() {
        return rootMethodNext;
    }
    
    public void setRootMethodNext(boolean rootMethodNext) {
        this.rootMethodNext = rootMethodNext;
    }
    
    public List<String> getJarDirList() {
        return jarDirList;
    }

    public void setJarDirList(List<String> jarDirList) {
        this.jarDirList = jarDirList;
    }

    public Set<String> getNeedHandlePackageSet() {
        return needHandlePackageSet;
    }

    public void setNeedHandlePackageSet(Set<String> needHandlePackageSet) {
        this.needHandlePackageSet = needHandlePackageSet;
    }
    
    /**
     * 处理方法调用时是否解析可能的类型与值
     * @return {@code true} 解析 {@code flase} 不解析
     */
    public boolean isParseMethodCallTypeValue() {
        return parseMethodCallTypeValue;
    }

    public void setParseMethodCallTypeValue(boolean parseMethodCallTypeValue) {
        this.parseMethodCallTypeValue = parseMethodCallTypeValue;
    }

    public boolean isFirstParseInitMethodType() {
        return firstParseInitMethodType;
    }

    public void setFirstParseInitMethodType(boolean firstParseInitMethodType) {
        this.firstParseInitMethodType = firstParseInitMethodType;
    }

    public boolean isContinueWhenError() {
        return continueWhenError;
    }

    public void setContinueWhenError(boolean continueWhenError) {
        this.continueWhenError = continueWhenError;
    }

    public boolean isDebugPrint() {
        return debugPrint;
    }

    public void setDebugPrint(boolean debugPrint) {
        this.debugPrint = debugPrint;
    }

    public boolean isDebugPrintInFile() {
        return debugPrintInFile;
    }

    public void setDebugPrintInFile(boolean debugPrintInFile) {
        this.debugPrintInFile = debugPrintInFile;
    }

    public boolean isLogMethodSpendTime() {
        return logMethodSpendTime;
    }

    public void setLogMethodSpendTime(boolean logMethodSpendTime) {
        this.logMethodSpendTime = logMethodSpendTime;
    }

    public String getOutputRootPath() {
        return outputRootPath;
    }

    public void setOutputRootPath(String outputRootPath) {
        this.outputRootPath = outputRootPath;
    }

    public String getOutputFileExt() {
        return outputFileExt;
    }

    public void setOutputFileExt(String outputFileExt) {
        this.outputFileExt = outputFileExt;
    }
    
    public List<String> getSourcesDirList() {
        return sourcesDirList;
    }
    
    public void setSourcesDirList(List<String> sourcesDirList) {
        this.sourcesDirList = sourcesDirList;
    }
}
