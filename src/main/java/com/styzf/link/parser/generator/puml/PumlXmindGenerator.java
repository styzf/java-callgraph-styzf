package com.styzf.link.parser.generator.puml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.method.MethodCallTree;
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
            if (StrUtil.isNotBlank(prev.genCallerFullMethod())
                    && prev.genCallerFullMethod().matches("^(java.lang.Object|java.lang.StringBuilder).+")) {
                return false;
            }
            addData(prev.genCallerFullMethod(), level);
            
            return true;
        }
        
        @Override
        protected boolean nextHandle(String prevMethodName, MethodCall next, int level) {
            if (StrUtil.isNotBlank(next.genCalleeFullMethod())
                    && next.genCalleeFullMethod().matches("^(java|java.lang.Object|java.lang.StringBuilder).+")) {
                return false;
            }
            addData(next.genCalleeFullMethod(), level);
            
            // java原生的方法不进行下一层解析，理论上来说也不存在这种情况
            return !next.genCalleeFullMethod().matches("^(java|styzf).+");
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
