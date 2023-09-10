package com.styzf.link.parser.dto.element.constant;

import com.styzf.link.parser.common.enums.JavaCGConstantTypeEnum;

/**
 * @author adrninistrator
 * @date 2022/5/13
 * @description:
 */
public class ConstElementLong extends ConstElement {

    public ConstElementLong(Object value) {
        super(value);
    }

    @Override
    public JavaCGConstantTypeEnum getConstantTypeEnum() {
        return JavaCGConstantTypeEnum.CONSTTE_LONG;
    }
}
