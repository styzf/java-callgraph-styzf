package com.styzf.link.parser.util.doc;

public class ClassToSrc {

    private ClassToSrc() {}

    public static final String SRC_MAIN_JAVA = "src/main/java";
    private static final String SRC_TEST_JAVA = "src/test/java";

    private static final String TARGET_CLASSES = "target/classes";
    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private static final String BUILD_CLASSES_JAVA_MAIN = "build/classes/java/main";
    private static final String BUILD_CLASSES_JAVA_TEST = "build/classes/java/test";

    public static final String[][] MAVEN_MAIN_JAVA = {
            {SRC_MAIN_JAVA, TARGET_CLASSES},
    };

    public static final String[][] MAVEN_JAVA = {
            {SRC_MAIN_JAVA, TARGET_CLASSES},
            {SRC_TEST_JAVA, TARGET_TEST_CLASSES},
    };


    public static final String[][] GRADLE_MAIN_JAVA = {
            {SRC_MAIN_JAVA, BUILD_CLASSES_JAVA_MAIN},
    };

    public static final String[][] GRADLE_JAVA = {
            {SRC_MAIN_JAVA, BUILD_CLASSES_JAVA_MAIN},
            {SRC_TEST_JAVA, BUILD_CLASSES_JAVA_TEST},
    };

    public static String srcPath(String classPath, String[][] srcClassPairs) {
        if (srcClassPairs.length == 0) {
            srcClassPairs = JavaSrcUtils.SRC_CLASS_PAIRS;
        }
        for (String[] srcClassPair : srcClassPairs) {
            if (srcClassPair.length != 2) {
                throw new ArrayIndexOutOfBoundsException("srcClassPairs sub string length must be 2");
            }
            classPath = classPath.replace(srcClassPair[1], srcClassPair[0]);
        }
        return classPath;
    }
}
