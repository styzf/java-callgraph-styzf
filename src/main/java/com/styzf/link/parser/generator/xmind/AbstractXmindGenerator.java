package com.styzf.link.parser.generator.xmind;

import com.styzf.link.parser.context.DataContext;
import com.styzf.link.parser.dto.method.MethodCallTree;
import com.styzf.link.parser.generator.FileGenerate;
import org.xmind.core.*;
import org.xmind.core.style.IStyleSheet;

import java.io.File;

/**
 * Xmind基础实现
 * @author styzf
 * @date 2021/12/15 21:18
 */
public abstract class AbstractXmindGenerator implements FileGenerate<MethodCallTree> {
    
    protected final IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
    protected final IWorkbook workbook = workbookBuilder.createWorkbook();
    protected final ISheet primarySheet = workbook.getPrimarySheet();
    protected final IStyleSheet styleSheet = workbook.getStyleSheet();
    protected final ITopic rootTopic = primarySheet.getRootTopic();
    
    protected ITopic generateTopic(MethodCallTree tree) {
        ITopic topic = workbook.createTopic();
        String text = tree.getRootMethodName();
        topic.setTitleText(text);
        return topic;
    }
    
    public void generateXmind(MethodCallTree tree) {
        try {
            String outputRootPath = DataContext.javaCGConfInfo.getOutputRootPath();
            // 后缀大小写不对会导致打开软件没打开文件
            String path = new File(outputRootPath  + File.separator + "xmind" + File.separator,
                    tree.getEasyMethodName().replaceAll("[\\.(|)|:|<|>]","_") + "." + XMindConstant.XMIND)
                    .getCanonicalPath();
            workbook.save(path);
        } catch (Exception ignored) {
            System.out.println(tree.getRootMethodName().replaceAll("[\\.(|)|:|<|>|,]","_"));
            ignored.printStackTrace();
        }
    }
}
