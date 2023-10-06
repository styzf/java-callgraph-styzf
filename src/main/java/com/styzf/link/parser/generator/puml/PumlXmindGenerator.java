package com.styzf.link.parser.generator.puml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.OldDataContext;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.generator.FilterUtil;
import com.styzf.link.parser.parser.AbstractLinkParser;
import com.styzf.link.parser.util.BaseUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author styzf
 * @date 2023/9/9 3:09
 */
public class PumlXmindGenerator extends AbstractPumlGenerator {
    private String easyMethodName;
    
    @Override
    public void generate() {
        PumlXmindParser pumlXmindParser = new PumlXmindParser();
        pumlXmindParser.parser();
        writer(FileUtil.getLineSeparator() + "@endmindmap");
        close();
    }
    
    @Override
    protected void setWriter() {
        String outputRootPath = OldDataContext.javaCGConfInfo.getOutputRootPath() + File.separator + "puml" + File.separator;
        String fileName = this.easyMethodName.replaceAll("[\\.(|)|:|<|>]","_") + PumlConstant.PUML;
        try {
            writer = IoUtil.getUtf8Writer(new FileOutputStream(outputRootPath + fileName));
        } catch (FileNotFoundException e) {e.printStackTrace();}
    }
    
    protected void setEasyMethodName(String easyMethodName) {
        this.easyMethodName = easyMethodName;
    }
    
    /**
     * 链路解析器
     */
    private class PumlXmindParser extends AbstractLinkParser {
        
        @Override
        protected String rootHandle(String rootMethodName) {
            rootMethodName = OldDataContext.getRootMethodName(rootMethodName);
            String easyMethodName = BaseUtil.getEasyMethodName(rootMethodName);
            setEasyMethodName(easyMethodName);
            setWriter();
            writer("@startmindmap");
            addData(rootMethodName, 0);
            return rootMethodName;
        }
        
        @Override
        protected boolean prevHandle(String nextMethodName, MethodCall prev, int level) {
            if (!FilterUtil.filter(prev.genCallerFullMethod())) {
                return false;
            }
            addData(prev.genCallerFullMethod(), level);
            
            return FilterUtil.filterNext(prev.genCallerFullMethod());
        }
        
        @Override
        protected boolean nextHandle(String prevMethodName, MethodCall next, int level) {
            if (!FilterUtil.filter(next.genCalleeFullMethod())) {
                return false;
            }
            addData(next.genCalleeFullMethod(), level);
            
            return FilterUtil.filterNext(next.genCalleeFullMethod());
        }
    
        @Override
        protected void loopHandle(String methodName, int level) {
            writer("\uD83D\uDD04");
        }
    };
    
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
