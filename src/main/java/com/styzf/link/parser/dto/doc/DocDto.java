package com.styzf.link.parser.dto.doc;

import java.util.Objects;

/**
 * @author styzf
 * @date 2023/9/11 23:00
 */
public class DocDto {
    
    public DocDto(String fullMethod) {
        this.fullMethod = fullMethod;
    }
    
    /**
     * 注释
     */
    private String doc = "";
    
    /**
     * 全名
     */
    private String fullMethod = "";
    
    public String getDoc() {
        return doc;
    }
    
    public void setDoc(String doc) {
        this.doc = doc;
    }
    
    public String getFullMethod() {
        return fullMethod;
    }
    
    public void setFullMethod(String fullMethod) {
        this.fullMethod = fullMethod;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocDto docDto = (DocDto) o;
        return Objects.equals(fullMethod, docDto.fullMethod);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fullMethod);
    }
}
