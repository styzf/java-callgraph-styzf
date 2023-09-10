//package test.config;
//
//import com.styzf.link.parser.common.JavaCGConstants;
//import com.styzf.link.parser.common.enums.JavaCGConfigKeyEnum;
//import com.styzf.link.parser.common.enums.JavaCGOtherConfigFileUseListEnum;
//import com.styzf.link.parser.conf.JavaCGConfigureWrapper;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Collections;
//
///**
// * @author adrninistrator
// * @date 2022/11/7
// * @description:
// */
//public class TestConfig {
//
//    private final JavaCGConfigureWrapper javaCGConfigureWrapper = new JavaCGConfigureWrapper();
//
//    @Before
//    public void init() {
//        javaCGConfigureWrapper.setOtherConfigList(JavaCGOtherConfigFileUseListEnum.OCFULE_JAR_DIR, Collections.singletonList("build/libs/java-callgraph2-0.2.2.jar"));
//    }
//
//    @Test
//    public void testDefault() {
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_PARSE_METHOD_CALL_TYPE_VALUE, Boolean.TRUE.toString());
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_FIRST_PARSE_INIT_METHOD_TYPE, Boolean.TRUE.toString());
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_CONTINUE_WHEN_ERROR, Boolean.FALSE.toString());
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_DEBUG_PRINT, Boolean.FALSE.toString());
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_OUTPUT_FILE_EXT, ".txt");
//        new JCallGraph().run(javaCGConfigureWrapper);
//    }
//
//    @Test
//    public void testDebugPrintOn() {
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_DEBUG_PRINT, Boolean.TRUE.toString());
//        new JCallGraph().run(javaCGConfigureWrapper);
//    }
//
//    @Test
//    public void testDebugPrintInFile() {
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_DEBUG_PRINT, JavaCGConstants.PROPERTY_VALUE_DEBUG_PRINT_IN_FILE);
//        new JCallGraph().run(javaCGConfigureWrapper);
//    }
//
//    @Test
//    public void testParseMethodCallTypeValueOff() {
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_DEBUG_PRINT, "");
//        javaCGConfigureWrapper.setConfig(JavaCGConfigKeyEnum.CKE_PARSE_METHOD_CALL_TYPE_VALUE, Boolean.FALSE.toString());
//        new JCallGraph().run(javaCGConfigureWrapper);
//    }
//}
