package com.styzf.link.parser.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.doc.DocDto;
import com.styzf.link.parser.util.doc.JavaDocUtils;
import com.sun.javadoc.MethodDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author styzf
 * @date 2023/9/11 22:56
 */
public class DocParser implements ParserInterface {
    private static final Logger LOG = LoggerFactory.getLogger(DocParser.class);
    private static JarClassLoader jarClassLoader;
    private static Set<String> CLASS_NAME_SET = new HashSet<>();
    
    /**
     * 解析文档注释
     */
    @Override
    public void parser() {
        getJarClassLoader();
        List<String> list = DataContext.HANDLED_CLASS_NAME_MAP.values()
                .stream()
                .flatMap(Collection::stream)
                .map(str -> str.replaceAll("/", ".").replace(".class", ""))
                .collect(Collectors.toList());
        for (String className : list) {
            try {
                parser(className);
            } catch (Throwable ignored) {
                LOG.error("解析失败：" + className);
            }
        }
    }
    
    public static void parser(String className) throws ClassNotFoundException {
        if (!CLASS_NAME_SET.add(className)) {
            return;
        }
        Class<?> clzss = jarClassLoader.loadClass(className);
        Method[] methods = clzss.getMethods();
        try {
            methods = ReflectUtil.getMethods(clzss);
        } catch (Throwable ignored) {
            LOG.error("解析失败：" + className);
        }
        if (ArrayUtil.isEmpty(methods)) {
            return;
        }
        for (Method method: methods) {
            String key = getKey(method);
            DocDto doc = new DocDto(key);
            DataContext.DOC_MAP.put(key, doc);
    
            try {
                MethodDoc methodDoc = JavaDocUtils.methodDoc(method);
                if (methodDoc == null) {
                    continue;
                }
                doc.setDoc(methodDoc.getRawCommentText());
            } catch (Throwable ignored) { }
        }
    }
    
    private static String getKey(Method method) {
        String key = method.toGenericString();
        if (key.contains(" throws ")) {
            key = key.substring(0, key.indexOf(" throws "));
        }
        if (key.contains(" ")) {
            key = key.substring(key.lastIndexOf(" ") + 1);
        }
        String[] split = key.split("\\.");
        key = "";
        for (String s : split) {
            if (s.startsWith(method.getName())) {
                key = key + ":" + s;
            } else {
                key = key + "." + s;
            }
        }
        key = key.replaceFirst("\\.", "");
        return key;
    }
    
    /**
     * 添加用于解析的 jar 包
     * <br>路径获取命令：<br>
     * mvn dependency:build-classpath
     */
    public static void getJarClassLoader() {
        List<String> jarDirList = DataContext.javaCGConfInfo.getJarDirList();
        for (String pathToJar : jarDirList) {
            if (StrUtil.isBlank(pathToJar)) {
                continue;
            }
            if (pathToJar.startsWith("null")) {
                pathToJar = pathToJar.replace("null", "");
            }
            if (File.separatorChar == '\\') {
                pathToJar = pathToJar.replace('/', File.separatorChar);
            }
            jarClassLoader = ClassLoaderUtil.getJarClassLoader(FileUtil.file(pathToJar));
        }
    }
}
