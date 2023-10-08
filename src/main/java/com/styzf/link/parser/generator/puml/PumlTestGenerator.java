package com.styzf.link.parser.generator.puml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.parser.AbstractLinkParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author styzf
 * @date 2023/9/9 3:09
 */
public class PumlTestGenerator extends AbstractPumlGenerator {
    private String easyMethodName;
    
    private AbstractLinkParser parser;
    
    @Override
    public void generate() {
        parser.parser();
        writer(FileUtil.getLineSeparator() + "@endmindmap");
        close();
    }
    
    public PumlTestGenerator setParser(AbstractLinkParser parser) {
        this.parser = parser;
        return this;
    };
    
    @Override
    protected void setWriter() {
        String outputRootPath = DataContext.javaCGConfInfo.getOutputRootPath() + File.separator + "puml" + File.separator;
        String fileName = this.easyMethodName.replaceAll("[\\.(|)|:|<|>]","_") + PumlConstant.PUML;
        try {
            writer = IoUtil.getUtf8Writer(new FileOutputStream(outputRootPath + fileName));
        } catch (FileNotFoundException e) {e.printStackTrace();}
    }
    
    protected void setEasyMethodName(String easyMethodName) {
        this.easyMethodName = easyMethodName;
    }
    private void addData(String methodName, int level) {
        String line = StrUtil.fillBefore("", '*', level + 1);
        if (level > 1) {
            line = line + "_ ";
        } else if (level == 0) {
            line = line + "[#orange] ";
        } else if (level == 1) {
            line = line + "[#66ccff] ";
        }
        writer(FileUtil.getLineSeparator() + line + methodName);
    }
}
