package com.styzf.link.parser.dto.element.constant;

import com.styzf.link.parser.common.enums.JavaCGConstantTypeEnum;

/**
 * @author adrninistrator
 * @date 2022/5/14
 * @description:
 */
public class ConstElementShort extends ConstElement {

    public ConstElementShort(Object value) {
        super(value);
    }

    @Override
    public JavaCGConstantTypeEnum getConstantTypeEnum() {
        return JavaCGConstantTypeEnum.CONSTTE_SHORT;
    }
}
