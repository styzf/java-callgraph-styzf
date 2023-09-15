package com.styzf.link.parser.generator;

import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.method.MethodCallTree;

import java.util.List;

/**
 * 文件生成器
 * @author styzf
 * @date 2021/12/15 20:42
 */
public interface FileGenerate {
    /**
     * 文件生成
     */
    void generate();
}
