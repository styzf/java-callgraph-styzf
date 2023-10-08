package com.styzf.link.parser.dto.jar;

import com.styzf.link.parser.context.DataContext;

/**
 * @author adrninistrator
 * @date 2022/2/13
 * @description: jar包信息
 */
public class JarInfo {
    private final int jarNum;

    private final String jarType;

    private final String jarPath;

    public JarInfo(int jarNum, String jarType, String jarPath) {
        this.jarNum = jarNum;
        this.jarType = jarType;
        this.jarPath = jarPath;
        DataContext.CLASS_AND_JAR_NUM.putJarInfo(this);
    }

    //
    public int getJarNum() {
        return jarNum;
    }

    public String getJarType() {
        return jarType;
    }

    public String getJarPath() {
        return jarPath;
    }
}
