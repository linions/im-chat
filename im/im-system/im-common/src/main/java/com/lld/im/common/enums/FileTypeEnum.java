package com.lld.im.common.enums;

public enum FileTypeEnum {


    TXT(1,"txt"),

    WORD(1,"word"),

    EXCEL(2,"excel"),

    PNG(2,"png"),

    PPT(2,"ppt"),

    PDF(2,"pdf"),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static FileTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < FileTypeEnum.values().length; i++) {
            if (FileTypeEnum.values()[i].getCode() == ordinal) {
                return FileTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    private String value;

    FileTypeEnum(int code, String value){
        this.code=code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
