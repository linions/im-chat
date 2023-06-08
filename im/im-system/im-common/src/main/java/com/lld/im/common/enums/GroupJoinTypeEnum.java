package com.lld.im.common.enums;

public enum GroupJoinTypeEnum {

    /**
     * 是否全员禁言，0 不禁言；1 全员禁言。
     */
    OWNER_CREATE(0,"群主创建"),

    MEMBER_INVITE(1,"群员邀请"),

    ACCOUNT_SEARCH(2,"群号搜索"),



    FORBID_ANY_MEMBER(0,"禁止任何人加入群聊"),

    MANAGER_APPROVE(1,"管理员同意"),

    ANY_MEMBER(2,"任何人都可以加入群聊"),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static GroupJoinTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupJoinTypeEnum.values().length; i++) {
            if (GroupJoinTypeEnum.values()[i].getCode() == ordinal) {
                return GroupJoinTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    private String value;

    GroupJoinTypeEnum(int code,String value){
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
