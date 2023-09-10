package com.styzf.link.parser.generator.puml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.dto.method.MethodCallTree;
import org.xmind.core.ITopic;

import java.util.List;

/**
 * @author styzf
 * @date 2023/9/9 3:09
 */
public class PumlXmindGenerator extends AbstractPumlGenerator {
    
    @Override
    public void generate(MethodCallTree tree) {
        lines.add("@startmindmap");
        addData(tree);
        generateCall(tree);
        lines.add("@endmindmap");
        writer(tree);
    }
    
    private void addData(MethodCallTree tree) {
        String line = StrUtil.fillBefore("", '*', tree.getLevel() + 1);
        if (tree.getLevel() > 1) {
            line = line + "_ ";
        } else if (tree.getLevel() == 0) {
            line = line + "[#orange] ";
        } else if (tree.getLevel() == 1) {
            line = line + "[#66ccff] ";
        }
        lines.add(line + tree.getRootMethodName());
    }
    
    private void generateCall(MethodCallTree data) {
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
            addData(next);
            
            // java原生的方法不进行下一层解析，理论上来说也不存在这种情况
            if (StrUtil.isNotBlank(rootMethodName)
                    && rootMethodName.matches("^(java|styzf).+")) {
                continue;
            }
            generateCall(next);
        }
    }
}
