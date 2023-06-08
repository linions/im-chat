package com.lld.im.common.enums;

public enum RequestTypeEnum {

    /**
     * 请求类型 1好友（类似微信） 2群
     */
    FRIEND(1),

    GROUP(2),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static RequestTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < RequestTypeEnum.values().length; i++) {
            if (RequestTypeEnum.values()[i].getCode() == ordinal) {
                return RequestTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    RequestTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
