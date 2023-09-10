package com.styzf.link.parser.parser;

import cn.hutool.core.util.ObjectUtil;
import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.method.MethodCallTree;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author styzf
 * @date 2023/9/7 22:17
 */
public class DataParser {
    public static void praserMethodCallLinked() {
        String rootMethodName = DataContext.javaCGConfInfo.getRootMethodName();
        MethodCallTree root = MethodCallTree.init(rootMethodName);
        if (DataContext.javaCGConfInfo.isRootMethodNext()) {
            root.nextParser();
            DataContext.root = root;
        } else {
            // TODO 向上解析
        }
    }
    
    /**
     * 解析所有无接口调用的数据
     */
    public static void praserAll() {
        Set<String> keySet = DataContext.METHOD_CALL_MAP.keySet();
        List<String> topMethod = keySet.stream()
                .filter(key -> !key.contains(":<init>")
                        && ObjectUtil.isNull(DataContext.METHOD_CALLEE_MAP.get(key)))
                .collect(Collectors.toList());
        for (String rootMethodName:topMethod) {
            MethodCallTree root = MethodCallTree.init(rootMethodName);
            root.nextParser();
            DataContext.rootList.add(root);
        }
    }
}
