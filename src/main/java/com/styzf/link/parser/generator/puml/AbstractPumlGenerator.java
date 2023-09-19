package com.styzf.link.parser.generator.puml;

import com.styzf.link.parser.generator.FileGenerate;

import java.io.IOException;
import java.io.Writer;

/**
 * @author styzf
 * @date 2023/9/9 3:09
 */
public abstract class AbstractPumlGenerator implements FileGenerate {
    protected Writer writer;
    
    /**
     * 设置写入流
     */
    protected abstract void setWriter();
    
    protected void writer(String line) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(line);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void close() {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
