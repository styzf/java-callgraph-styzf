package com.styzf.link.parser.generator.puml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.method.MethodCallTree;
import com.styzf.link.parser.generator.FilterUtil;
import com.styzf.link.parser.parser.AbstractLinkParser;
import com.styzf.link.parser.util.BaseUtil;
import org.xmind.core.ITopic;

import java.util.List;

/**
 * @author styzf
 * @date 2023/9/9 3:09
 */
public class PumlXmindGenerator extends AbstractPumlGenerator {
    
    @Override
    public void generate() {
        lines.add("@startmindmap");
        PumlXmindParser pumlXmindParser = new PumlXmindParser();
        pumlXmindParser.parser();
        lines.add("@endmindmap");
        writer(pumlXmindParser.getEasyMethodName());
    }
    
    /**
     * 链路解析器
     */
    private class PumlXmindParser extends AbstractLinkParser {
        protected String rootMethodName;
        
        @Override
        protected String rootHandle(String rootMethodName) {
            this.rootMethodName = DataContext.getRootMethodName(rootMethodName);
            addData(this.rootMethodName, 0);
            return this.rootMethodName;
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
            addData(methodName + "\uD83D\uDD04", level);
        }
    
        protected String getEasyMethodName() {
            return BaseUtil.getEasyMethodName(this.rootMethodName);
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
            lines.add(line + methodName);
        }
    };
}
