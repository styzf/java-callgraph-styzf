package com.styzf.link.parser.generator.xmind;

import cn.hutool.core.util.StrUtil;
import com.styzf.link.parser.context.OldDataContext;
import com.styzf.link.parser.dto.doc.DocDto;
import com.styzf.link.parser.generator.FileGenerate;
import com.styzf.link.parser.util.BaseUtil;
import com.styzf.link.parser.util.doc.MyJavaDocUtils;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookBuilder;
import org.xmind.core.style.IStyleSheet;

import java.io.File;

/**
 * Xmind基础实现
 * @author styzf
 * @date 2021/12/15 21:18
 */
public abstract class AbstractXmindGenerator implements FileGenerate {
    
    protected final IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
    protected final IWorkbook workbook = workbookBuilder.createWorkbook();
    protected final ISheet primarySheet = workbook.getPrimarySheet();
    protected final IStyleSheet styleSheet = workbook.getStyleSheet();
    protected final ITopic rootTopic = primarySheet.getRootTopic();
    
    protected ITopic generateTopic(String methodName) {
        ITopic topic = workbook.createTopic();
        setDoc(methodName, topic);
        
        return topic;
    }
    
    /**
     * 设置注释
     * @param methodName 方法全名
     * @param topic 需要设置注释的Topic
     */
    protected void setDoc(String methodName, ITopic topic) {
        DocDto doc = MyJavaDocUtils.getDoc(methodName);
        if (doc != null && StrUtil.isNotBlank(doc.getDoc())) {
            topic.setTitleText(doc.getDoc());
            topic.addLabel(methodName);
        } else {
            topic.setTitleText(methodName);
        }
    }
    
    /**
     * 生成xmind文件
     * @param methodName 方法全名
     */
    public void generateXmind(String methodName) {
        try {
            String outputRootPath = OldDataContext.javaCGConfInfo.getOutputRootPath();
            // 后缀大小写不对会导致打开软件没打开文件
            String path = new File(outputRootPath  + File.separator + "xmind" + File.separator,
                    BaseUtil.getEasyMethodName(methodName)
                            .replaceAll("[\\.(|)|:|<|>]","_")
                            + "." + XMindConstant.XMIND)
                    .getCanonicalPath();
            workbook.save(path);
        } catch (Exception e) {
            System.out.println(methodName);
        }
    }
    
}
