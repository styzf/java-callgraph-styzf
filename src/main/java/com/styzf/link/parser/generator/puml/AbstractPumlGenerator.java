package com.styzf.link.parser.generator.puml;

import cn.hutool.core.io.file.FileWriter;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.method.MethodCallTree;
import com.styzf.link.parser.generator.FileGenerate;
import com.styzf.link.parser.parser.AbstractLinkParser;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author styzf
 * @date 2023/9/9 3:09
 */
public abstract class AbstractPumlGenerator implements FileGenerate {
    
    protected List<String> lines = new LinkedList<>();
    
    protected void writer(String easyMethodName) {
        String outputRootPath = DataContext.javaCGConfInfo.getOutputRootPath() + File.separator + "puml" + File.separator;
        String fileName = easyMethodName.replaceAll("[\\.(|)|:|<|>]","_") + PumlConstant.PUML;
        new FileWriter(outputRootPath + fileName).writeLines(lines);
    }
}
