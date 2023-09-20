package com.styzf.link.parser.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * maven处理工具，用于解析maven项目
 */
public class MavenUtils {

    private MavenUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(MavenUtils.class);

    static {
        // 兼容环境变量不是 M2_HOME 而是 MAVEN_HOME
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null) {
            System.setProperty("maven.home", mavenHome);
        }
    }

    /**
     * 获取依赖 jar
     */
    public static String getDep(File pomFile) {
        DepHandler handler = new DepHandler();
        run(pomFile, handler, Collections.singletonList("dependency:build-classpath"));
        return handler.output;
    }
    
    /**
     * 获取依赖 jar
     */
    public static List<String> getDepList(File pomFile) {
        DepHandler handler = new DepHandler();
        run(pomFile, handler, Collections.singletonList("dependency:build-classpath"));
        
        return CollUtil.toList(handler.output.split(";"));
    }
    
    /**
     * 向上查找 pom 文件
     */
    public static File parentPomFile(File path) {
        // 避免相对路径 getParentFile 为空
        path = path.getAbsoluteFile();
        File pomFile = new File(path, "pom.xml");
        while (!pomFile.exists()) {
            path = path.getParentFile();
            if (path == null) {
                return null;
            }
            pomFile = new File(path, "pom.xml");
        }
        return pomFile;
    }

    /**
     * 执行 Maven 命令
     */
    public static void run(File pomFile, InvocationOutputHandler outputHandler, List<String> goals) {
        if (pomFile == null || goals == null || goals.isEmpty()) {
            return;
        }
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(goals);
        request.setOutputHandler(outputHandler);
        request.setBatchMode(true);
        Invoker invoker = new DefaultInvoker();
        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            LOG.error("pomFile:\n{}", goals);
            LOG.error("goals fail:\t{}", goals, e);
        }
    }
}
