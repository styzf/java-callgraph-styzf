package com.styzf.link.parser.dto.instruction;

import com.styzf.link.parser.dto.element.BaseElement;

/**
 * @author adrninistrator
 * @date 2022/11/4
 * @description: return类指令的解析结果
 */
public class ReturnParseResult extends BaseInstructionParseResult {

    // 返回信息
    private final BaseElement returnInfo;

    public ReturnParseResult(BaseElement returnInfo) {
        this.returnInfo = returnInfo;
    }

    public BaseElement getReturnInfo() {
        return returnInfo;
    }
}
