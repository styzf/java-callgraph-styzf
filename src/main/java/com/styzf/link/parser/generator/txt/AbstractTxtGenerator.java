package com.styzf.link.parser.generator.txt;

import com.styzf.link.parser.common.enums.JavaCGOutPutFileTypeEnum;
import com.styzf.link.parser.generator.FileGenerate;
import com.styzf.link.parser.util.JavaCGFileUtil;

import java.io.Writer;

/**
 * @author styzf
 * @date 2023/9/14 22:55
 */
public abstract class AbstractTxtGenerator implements FileGenerate {
    protected Writer writer = getWriter();
    
    protected abstract Writer getWriter();
}
