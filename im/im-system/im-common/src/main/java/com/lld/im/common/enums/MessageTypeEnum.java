package com.lld.im.common.enums;

public enum MessageTypeEnum {

    /**
     * 文本
     */
    TEXT(1),

    /**
     * 图片/文件、文件夹
     */
    FILE(2),

    ;

    public static MessageTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < MessageTypeEnum.values().length; i++) {
            if (MessageTypeEnum.values()[i].getCode() == ordinal) {
                return MessageTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    MessageTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
