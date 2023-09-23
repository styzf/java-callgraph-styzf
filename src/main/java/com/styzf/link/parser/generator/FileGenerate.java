package com.styzf.link.parser.generator;

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
    
    /**
     * 文件生成，并且计算时长
     */
    default void generateCalcTime() {
        long start = System.currentTimeMillis();
        this.generate();
        long end = System.currentTimeMillis();
        System.out.println(this.getClass().toString() + "耗时: " + ((end - start) / 1000.0D) + " S");
    };
}
