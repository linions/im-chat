package com.lld.im.common.enums;

public enum RoleTypeEnum {

    /**
     * 普通管理员
     */
    ADMIN("管理员"),

    /**
     * 超级管理员
     */
    SUPER_ADMIN("超级管理员"),

    /**
     * 普通用户
     */
    COMMON("普通用户"),


    ;

    private String value;

    RoleTypeEnum(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
