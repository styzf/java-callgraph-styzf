package com.styzf.link.parser.util.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.CodeSource;

public class JavaSrcUtils {

    private JavaSrcUtils() {}

    public static String[][] SRC_CLASS_PAIRS = ClassToSrc.MAVEN_JAVA;

    private static final Logger LOG = LoggerFactory.getLogger(JavaSrcUtils.class);

    public static Class<?> fileClass(Class<?> clazz) {
        Class<?> enclosingClass;
        while ((enclosingClass = clazz.getEnclosingClass()) != null) {
            clazz = enclosingClass;
        }
        return clazz;
    }

    public static String srcText(Class<?> clazz, String[]... srcClassPairs) {
        clazz = fileClass(clazz);
        String filePath = srcFile(clazz);
        String classDir = classDir(clazz);
        if (classDir == null) {
            return null;
        }
        if (classDir.endsWith(".jar")) {
            String jarPath = classDir.substring(0, classDir.length() - 4).concat("-sources.jar");
            if (!new File(jarPath).exists()) {
                // auto down -source.jar
                return null;
            }
            return UnZipUtils.text(jarPath, filePath, StandardCharsets.UTF_8);
        } else if (classDir.endsWith("src.zip")) {
            return UnZipUtils.text(classDir, filePath, StandardCharsets.UTF_8);
        } else {
            String srcPath = ClassToSrc.srcPath(classDir, srcClassPairs);
            return text(srcPath, filePath);
        }
    }

    static String srcFile(Class<?> clazz) {
        return clazz.getTypeName().replace(".", "/") + ".java";
    }

    public static String classDir(Class<?> clazz) {
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            URL location = codeSource.getLocation();
            if (location == null) {
                LOG.warn("codeSource.getLocation() == null, codeSource:{}", codeSource);
                return null;
            }
            return location.getPath();
        }
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) {
            return null;
        }
        File file = new File(javaHome, "src.zip");
        if (!file.exists()) {
            return null;
        }
        return file.getPath();
    }

    protected static String text(String dirPath, String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(new File(dirPath, filePath).toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
