package com.styzf.link.parser.common.enums;

/**
 * @author adrninistrator
 * @date 2022/11/7
 * @description:
 */
public enum JavaCGConfigKeyEnum {
    CKE_PARSE_METHOD_CALL_TYPE_VALUE("parse.method.call.type.value", "处理方法调用时是否解析可能的类型与值"),
    CKE_FIRST_PARSE_INIT_METHOD_TYPE("first.parse.init.method.type", "处理类的方法前是否需要先解析构造函数以非静态字段可能的类型，仅当上面的参数为true时才可以生效"),
    CKE_CONTINUE_WHEN_ERROR("continue.when.error", "处理方法出现异常时，是否要继续"),
    CKE_DEBUG_PRINT("debug.print", "调试日志打印开关"),
    CKE_LOG_METHOD_SPEND_TIME("log.method.spend.time", "记录方法分析耗时的开关"),
    CKE_OUTPUT_ROOT_PATH("output.root.path", "生成文件的根目录，以\"/\"或\"\\\\\"作为分隔符，末尾是否为分隔符不影响（默认为jar包所在目录）"),
    CKE_OUTPUT_FILE_EXT("output.file.ext", "生成文件后缀名（默认为.txt）"),
    PARSER_ROOT_METHOD("parser.root.method", "要解析得根方法"),
    PARSER_ROOT_METHOD_NEXT("parser.root.method.next", "要解析得根方法的方向"),
    PARSER_FILTER_REGEX("parser.filter.regEx", "过滤不需要解析的正则表达式"),
    PARSER_FILTER_NEXT_REGEX("parser.filter.next.regEx", "过滤不需要向下解析的正则表达式"),
    ;

    private final String key;
    private final String desc;

    JavaCGConfigKeyEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return key;
    }
}
