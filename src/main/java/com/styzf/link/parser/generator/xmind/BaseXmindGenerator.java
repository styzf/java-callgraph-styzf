package com.styzf.link.parser.generator.xmind;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.dto.method.MethodCallTree;
import org.xmind.core.ITopic;

import java.util.List;

/**
 * @author styzf
 * @date 2021/12/15 21:54
 */
public class BaseXmindGenerator extends AbstractXmindGenerator {
    
    @Override
    public void generate(MethodCallTree tree) {
        addRoot(tree);
        generateXmind(tree);
    }
    
    public void generate(List<MethodCallTree> list) {
        list.forEach(this::generate);
    }
    
    private void addRoot(MethodCallTree tree) {
        ITopic iTopic = generateTopic(tree);
        rootTopic.add(iTopic);
        generateCallTopic(tree, iTopic);
    }
    
    private void generateCallTopic(MethodCallTree data, ITopic lastTopic) {
        List<MethodCallTree> nextList = data.getNextList();
        if (CollUtil.isEmpty(nextList)) {
            return;
        }
    
        for (MethodCallTree next: nextList) {
            String rootMethodName = next.getRootMethodName();
            // object实现的方法不做解析，字符串拼接也不解析
            if (StrUtil.isNotBlank(rootMethodName)
                    && rootMethodName.matches("^(java.lang.Object|java.lang.StringBuilder).+")) {
                continue;
            }
            ITopic iTopic = generateTopic(next);
            lastTopic.add(iTopic);
    
            // java原生的方法不进行下一层解析，理论上来说也不存在这种情况
            if (StrUtil.isNotBlank(rootMethodName)
                    && rootMethodName.matches("^(java|styzf).+")) {
                continue;
            }
            generateCallTopic(next, iTopic);
        }
    }
    
}
