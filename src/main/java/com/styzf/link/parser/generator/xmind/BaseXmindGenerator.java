package com.styzf.link.parser.generator.xmind;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.method.MethodCallTree;
import com.styzf.link.parser.generator.FilterUtil;
import com.styzf.link.parser.parser.AbstractLinkParser;
import org.xmind.core.ITopic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author styzf
 * @date 2021/12/15 21:54
 */
public class BaseXmindGenerator extends AbstractXmindGenerator {
    @Override
    public void generate() {
        XmindLinkParser xmindLinkParser = new XmindLinkParser();
        xmindLinkParser.parser();
        generateXmind(xmindLinkParser.getRootMethodName());
    }
    
    private class XmindLinkParser extends AbstractLinkParser {
        /**
         * 存储每一层的末级Topic
         */
        private final Map<Integer, ITopic> lastTopic = new HashMap<>();
        private String rootMethodName;
        
        @Override
        protected boolean nextHandle(String prevMethodName, MethodCall next, int level) {
            if (!FilterUtil.filter(next.genCalleeFullMethod())) {
                return false;
            }
            addTopic(next.genCalleeFullMethod(), level);
    
            return FilterUtil.filterNext(next.genCalleeFullMethod());
        }
    
        @Override
        protected String rootHandle(String rootMethodName) {
            this.rootMethodName = DataContext.getRootMethodName(rootMethodName);
            lastTopic.put(0, rootTopic);
            addTopic(this.rootMethodName, 0);
        
            return this.rootMethodName;
        }
    
        @Override
        protected boolean prevHandle(String nextMethodName, MethodCall prev, int level) {
            if (!FilterUtil.filter(prev.genCallerFullMethod())) {
                return false;
            }
            addTopic(prev.genCallerFullMethod(), level);
            
            return FilterUtil.filterNext(prev.genCallerFullMethod());
        }
        
        private void addTopic(String methodName, int level) {
            ITopic iTopic = generateTopic(methodName);
            lastTopic.put(level + 1, iTopic);
            ITopic prevTopic = lastTopic.get(level);
            prevTopic.add(iTopic);
        }
        
        protected String getRootMethodName() {
            return this.rootMethodName;
        }
    }
}
