package com.styzf.link.parser.util.doc;

import cn.hutool.core.io.FileUtil;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javadoc.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * by com.sun.javadoc
 * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/javadoc">
 * 官方文档</a>
 * <a href="https://openjdk.java.net/jeps/106">
 * JEP 106: Add Javadoc to javax.tools</a>
 */
public class JavaDocUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JavaDocUtils.class);
    private static final String CLASS_NAME = JavaDocUtils.class.getName();
    private static RootDoc rootDoc;
    
    private JavaDocUtils() {}

    /** 生成文档必要方法 */
    @SuppressWarnings("unused")
    public static boolean start(RootDoc rootDoc) {
        JavaDocUtils.rootDoc = rootDoc;
        return true;
    }

    public static MethodDoc methodDoc(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        ClassDoc classDoc = classDoc(clazz);
        if (classDoc == null) {
            return null;
        }
        String inputName = method.getName();
        for (MethodDoc methodDoc : classDoc.methods()) {
            String name = methodDoc.name();
            if (inputName.equals(name)) {
                return methodDoc;
            }
        }
        return null;
    }

    public static ClassDoc classDoc(Class<?> clazz, String[]... srcClassPairs) {
        Class<?> fileClass = JavaSrcUtils.fileClass(clazz);
        String filePath = JavaSrcUtils.srcFile(fileClass);
        String classDir = JavaSrcUtils.classDir(fileClass);
        if (classDir == null) {
            return null;
        }
        List<String> cmd = new ArrayList<>();
        cmd.add("-doclet");
        cmd.add(CLASS_NAME);
        cmd.add("-quiet");
        cmd.add("-private");
        cmd.add("-encoding");
        cmd.add("utf-8");
        if (classDir.endsWith(".jar")) {
            String srcPath = classDir.substring(0, classDir.length() - 4).concat("-sources.jar");
            if (!new File(srcPath).exists()) {
                // auto down -source.jar
                return null;
            }
            if (unZipToCmd(cmd, srcPath, filePath)) {
                return null;
            }
        } else if (classDir.endsWith("src.zip")) {
            if (unZipToCmd(cmd, classDir, filePath)) {
                return null;
            }
        } else {
            String srcPath = ClassToSrc.srcPath(classDir, srcClassPairs);
            cmd.add(srcPath + filePath);
            Main.execute(cmd.toArray(new String[0]));
        }
        if (rootDoc == null) {
            return null;
        }
        ClassDoc[] classDocs = rootDoc.classes();
        String inputName = clazz.getName();
        for (ClassDoc classDoc : classDocs) {
            String name = classDoc.qualifiedName();
            if (inputName.equals(name)) {
                return classDoc;
            }
        }
        return null;
    }

    private static boolean unZipToCmd(List<String> cmd, String srcPath, String filePath) {
        String text = UnZipUtils.text(srcPath, filePath, StandardCharsets.UTF_8);
        if (text == null) {
            return true;
        }
        String srcUnZipRoot = srcPath.substring(0, srcPath.length() - 4);
        String unZipPath = srcUnZipRoot + '/' + filePath;
        File file = new File(unZipPath);
        File parentFile = file.getParentFile();
        
        FileUtil.writeString(text, file, StandardCharsets.UTF_8);
        cmd.add(unZipPath);
        Main.execute(cmd.toArray(new String[0]));
        file.deleteOnExit();
        return false;
    }
}
