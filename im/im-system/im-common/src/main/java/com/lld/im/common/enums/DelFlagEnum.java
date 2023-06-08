package com.lld.im.common.enums;

public enum DelFlagEnum {

    /**
     * 0 正常；1 删除。
     */
    NORMAL(0),

    DELETE(1),
    ;

    private Integer code;

    DelFlagEnum(int code){
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }
}
