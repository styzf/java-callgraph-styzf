package com.styzf.link.parser.generator.txt;

import com.styzf.link.parser.common.enums.JavaCGOutPutFileTypeEnum;
import com.styzf.link.parser.context.OldDataContext;
import com.styzf.link.parser.dto.call.MethodCall;
import com.styzf.link.parser.dto.output.JavaCGOutputInfo;
import com.styzf.link.parser.util.JavaCGFileUtil;
import com.styzf.link.parser.util.JavaCGUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.util.Collection;

/**
 * @author styzf
 * @date 2023/9/15 1:32
 */
public class MethodCallTxtGeneratot extends AbstractTxtGenerator {
    @Override
    public void generate() {
        OldDataContext.METHOD_CALL_MAP.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted(MethodCall::compareTo)
                .forEach(methodCall -> {
                    String callerClassJarNum = OldDataContext.CLASS_AND_JAR_NUM.getJarNum(methodCall.getCallerClassName());
                    String calleeClassJarNum = OldDataContext.CLASS_AND_JAR_NUM.getJarNum(methodCall.getCalleeClassName());
                    JavaCGFileUtil.write2FileWithTab(writer, methodCall.genCallContent(callerClassJarNum, calleeClassJarNum));
                });
    }
    
    @Override
    protected Writer getWriter() {
        String outputRootPath = OldDataContext.javaCGConfInfo.getOutputRootPath();
        String dirPath = JavaCGUtil.addSeparator4FilePath(outputRootPath) + File.separator;
        JavaCGOutputInfo outputInfo = new JavaCGOutputInfo(dirPath, OldDataContext.javaCGConfInfo.getOutputFileExt());
        try {
            return JavaCGFileUtil.genBufferedWriter(outputInfo.getMainFilePath(JavaCGOutPutFileTypeEnum.OPFTE_METHOD_CALL));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("文件未找到");
        }
    }
}
