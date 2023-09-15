package com.styzf.link.parser.util.doc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.doc.DocDto;
import com.styzf.link.parser.util.BaseUtil;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javadoc.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * by com.sun.javadoc
 * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/javadoc">
 * 官方文档</a>
 * <a href="https://openjdk.java.net/jeps/106">
 * JEP 106: Add Javadoc to javax.tools</a>
 */
public class MyJavaDocUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MyJavaDocUtils.class);
    private static final String CLASS_NAME = MyJavaDocUtils.class.getName();
    private static RootDoc rootDoc;
    // 要进行解压的源包
    private static final Set<String> SOURCES_DIR_SET = new HashSet<>();
    private static final Set<String> Class_SET = new HashSet<>();
    
    private MyJavaDocUtils() {}

    /** 生成文档必要方法 */
    @SuppressWarnings("unused")
    public static boolean start(RootDoc rootDoc) {
        MyJavaDocUtils.rootDoc = rootDoc;
        return true;
    }
    
    public static DocDto getDoc(String methodName) {
        return getDoc(BaseUtil.getClassName(methodName), methodName);
    }
    
    public static DocDto getDoc(String className, String methodName) {
        if (! Class_SET.add(className)) {
            return getDocByMethodName(methodName);
        }
        ClassDoc classDoc = classDoc(className);
        if (classDoc == null) {
            return null;
        }
    
        for (MethodDoc methodDoc: classDoc.methods()) {
            String key = methodDoc.toString();
            String commentText = methodDoc.getRawCommentText();
            if (StrUtil.isBlank(commentText)) {
                continue;
            }
            
            key = key.replace(" ", "");
            DocDto doc = new DocDto(key);
            doc.setDoc(commentText);
            DataContext.DOC_MAP.put(key, doc);
            DataContext.DOC_MAP.put(key.substring(0, key.indexOf("(")), doc);
        }
        
        return getDocByMethodName(methodName);
    }
    
    private static DocDto getDocByMethodName(String methodName) {
        methodName = methodName.replace(":", ".");
        DocDto doc = DataContext.DOC_MAP.get(methodName);
        if (doc != null) {
            return doc;
        }
    
        return DataContext.DOC_MAP.get(methodName.substring(0, methodName.indexOf("(")));
    }
    
    public static ClassDoc classDoc(String className) {
        List<String> sourcesDirList = DataContext.javaCGConfInfo.getSourcesDirList();
        if (CollUtil.isEmpty(sourcesDirList)) {
            return null;
        }
    
        for (String sourcesDir:sourcesDirList) {
            List<String> cmd = new ArrayList<>();
            cmd.add("-doclet");
            cmd.add(CLASS_NAME);
            cmd.add("-quiet");
            cmd.add("-private");
            cmd.add("-encoding");
            cmd.add("utf-8");
            if (unZipToCmd(cmd, sourcesDir, className)) {
                continue;
            }
            
            if (rootDoc == null) {
                continue;
            }
            ClassDoc[] classDocs = rootDoc.classes();
            for (ClassDoc classDoc : classDocs) {
                String name = classDoc.qualifiedName();
                if (className.equals(name)) {
                    return classDoc;
                }
            }
        }
        
        return null;
    }

    private static boolean unZipToCmd(List<String> cmd, String sourcesDir, String filePath) {
        String classPath = filePath.replace(".", "/") + ".java";
        File sourcesFile = FileUtil.file(sourcesDir.substring(0, sourcesDir.indexOf(".jar")) + '/' + classPath);
        if (SOURCES_DIR_SET.add(sourcesFile.getPath())) {
            ZipUtil.unzip(FileUtil.file(sourcesDir));
        }
        if (!sourcesFile.exists()) {
            return true;
        }
        
        String text = FileUtil.readUtf8String(sourcesFile);
        if (StrUtil.isBlank(text)) {
            return true;
        }
        cmd.add(sourcesFile.getPath());
        Main.execute(cmd.toArray(new String[0]));
        
        return false;
    }
    
    public static void delete() {
        List<String> sourcesDirList = DataContext.javaCGConfInfo.getSourcesDirList();
        if (CollUtil.isEmpty(sourcesDirList)) {
            return;
        }
    
        for (String sourcesDir:sourcesDirList) {
            File file = new File(sourcesDir);
            if (file.exists()) {
                FileUtil.del(file);
            }
        }
    }
}
