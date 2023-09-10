package com.styzf.link.parser.extensions.manager;

import com.styzf.link.parser.dto.output.JavaCGOutputInfo;
import com.styzf.link.parser.exceptions.JavaCGRuntimeException;
import com.styzf.link.parser.extensions.annotation_attributes.AnnotationAttributesFormatterInterface;
import com.styzf.link.parser.extensions.annotation_attributes.DefaultAnnotationAttributesFormatter;
import com.styzf.link.parser.extensions.code_parser.CodeParserInterface;
import com.styzf.link.parser.extensions.code_parser.JarEntryOtherFileParser;
import com.styzf.link.parser.extensions.code_parser.MethodAnnotationParser;
import com.styzf.link.parser.extensions.code_parser.AbstractSaveData2FileParser;
import com.styzf.link.parser.extensions.code_parser.SpringXmlBeanParserInterface;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2023/2/6
 * @description: 扩展类管理类
 */
public class ExtensionsManager {

    // 保存所有的代码解析扩展类列表
    private final List<CodeParserInterface> allCodeParserList = new ArrayList<>();

    /*
        保存处理jar包中其他类型文件的扩展类
        key:
            对应的文件类型小写形式（包含"."，即格式为".xxx"）
        value:
            当前文件类型对应的扩展类列表（一种文件类型可能会有多个对应的扩展类）
     */
    private final Map<String, List<JarEntryOtherFileParser>> otherFileParserMap = new HashMap<>();

    /*
        保存处理方法注解的扩展类
        key:
            对应的注解类名
        value:
            当前注解类名对应的扩展类（一种文件类型只会有一个对应的扩展类）
     */
    private final Map<String, MethodAnnotationParser> methodAnnotationParserMap = new HashMap<>();

    // 解析并将结果保存在文件的类
    private final List<AbstractSaveData2FileParser> saveData2FileParserList = new ArrayList<>();

    // 对注解属性的元素值进行格式化的类
    private AnnotationAttributesFormatterInterface annotationAttributesFormatter;

    private SpringXmlBeanParserInterface springXmlBeanParser;

    private JavaCGOutputInfo javaCGOutputInfo;

    private boolean inited = false;

    /**
     * 添加代码解析扩展类
     *
     * @param codeParser
     */
    public void addCodeParser(CodeParserInterface codeParser) {
        allCodeParserList.add(codeParser);
    }

    // 初始化
    public boolean init() {
        if (inited) {
            throw new JavaCGRuntimeException("不允许重复初始化");
        }

        // 固定添加Spring XML Bean信息解析类
        if (springXmlBeanParser != null) {
            allCodeParserList.add(springXmlBeanParser);
        }

        for (CodeParserInterface codeParser : allCodeParserList) {
            // 初始化扩展类
            codeParser.initCodeParser();

            if (codeParser instanceof JarEntryOtherFileParser) {
                // 处理jar包中其他类型文件的扩展类
                JarEntryOtherFileParser jarEntryOtherFileParser = (JarEntryOtherFileParser) codeParser;
                String[] otherFileExtensions = jarEntryOtherFileParser.chooseJarEntryOtherFileExt();
                if (otherFileExtensions != null) {
                    for (String otherFileExtension : otherFileExtensions) {
                        List<JarEntryOtherFileParser> otherFileParserList = otherFileParserMap.computeIfAbsent(otherFileExtension, k -> new ArrayList<>());
                        otherFileParserList.add(jarEntryOtherFileParser);
                    }
                }

                // 处理解析并将结果保存在文件的类
                if (codeParser instanceof AbstractSaveData2FileParser) {
                    AbstractSaveData2FileParser saveData2FileParser = (AbstractSaveData2FileParser) codeParser;
                    // 增加其他文件信息
                    String outputFilePath = javaCGOutputInfo.addOtherFileInfo(saveData2FileParser.chooseFileName());
                    if (!saveData2FileParser.init(outputFilePath)) {
                        System.err.println("初始化失败 " + codeParser.getClass().getName());
                        return false;
                    }
                    saveData2FileParserList.add(saveData2FileParser);
                }
            } else if (codeParser instanceof MethodAnnotationParser) {
                // 处理方法注解的扩展类
                MethodAnnotationParser methodAnnotationParser = (MethodAnnotationParser) codeParser;
                String[] methodAnnotationClassNames = methodAnnotationParser.chooseMethodAnnotationClassName();
                if (methodAnnotationClassNames != null) {
                    for (String methodAnnotationClass : methodAnnotationClassNames) {
                        methodAnnotationParserMap.put(methodAnnotationClass, methodAnnotationParser);
                    }
                }
            } else {
                System.err.println("不支持的类型 " + codeParser.getClass().getName());
                return false;
            }
        }

        inited = true;
        return true;
    }

    // 关闭处理
    public void close() {
        for (AbstractSaveData2FileParser saveData2FileParser : saveData2FileParserList) {
            saveData2FileParser.close();
        }
    }

    private void checkInited() {
        if (!inited) {
            throw new JavaCGRuntimeException("还未完成初始化");
        }
    }

    public List<JarEntryOtherFileParser> getJarEntryOtherFileParserList(String fileExtension) {
        checkInited();
        if (StringUtils.isBlank(fileExtension)) {
            return null;
        }
        return otherFileParserMap.get(fileExtension);
    }

    public MethodAnnotationParser getMethodAnnotationParser(String methodAnnotationClass) {
        checkInited();
        if (StringUtils.isBlank(methodAnnotationClass)) {
            return null;
        }
        return methodAnnotationParserMap.get(methodAnnotationClass);
    }

    public AnnotationAttributesFormatterInterface getAnnotationAttributesFormatter() {
        if (annotationAttributesFormatter == null) {
            annotationAttributesFormatter = new DefaultAnnotationAttributesFormatter();
        }

        return annotationAttributesFormatter;
    }

    public SpringXmlBeanParserInterface getSpringXmlBeanParser() {
        return springXmlBeanParser;
    }

    public void setSpringXmlBeanParser(SpringXmlBeanParserInterface springXmlBeanParser) {
        this.springXmlBeanParser = springXmlBeanParser;
    }

    public void setAnnotationAttributesFormatter(AnnotationAttributesFormatterInterface annotationAttributesFormatter) {
        this.annotationAttributesFormatter = annotationAttributesFormatter;
    }

    public void setJavaCGOutputInfo(JavaCGOutputInfo javaCGOutputInfo) {
        this.javaCGOutputInfo = javaCGOutputInfo;
    }
}
