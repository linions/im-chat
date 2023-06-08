package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;


@Data
public class ModifyUserInfoReq extends RequestBase {

    // 用户id
    @NotEmpty(message = "用户id不能为空")
    @TableField(value = "user_id")
    private String userId;

    // 用户名称
    @TableField(value = "nick_name")
    private String nickName;

    // 性别
    @TableField(value = "user_sex")
    private Integer userSex;

    //生日
    @TableField(value = "birth_day")
    private Date birthDay;

    //位置
    @TableField(value = "location")
    private String location;

    // 个性签名
    @TableField(value = "self_signature")
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    @TableField(value = "friend_allow_type")
    private Integer friendAllowType;

    //密码
    @TableField(value = "mobile")
    private String mobile;

    @TableField(value = "code")
    private String code;

    private int  type;


    //密码
    @TableField(value = "email")
    private String email;

    //密码
    @TableField(value = "password")
    private String password;

    // 头像
    @TableField(value = "photo")
    private String photo;

    // 是否可以加好友 0否 1是
    @TableField(value = "disable_add_friend")
    private int disableAddFriend;

    // 禁言标识 1禁用 0未禁用
    @TableField(value = "silent_flag")
    private int silentFlag;

    // 禁用标识 1禁用 0未禁用
    @TableField(value = "forbidden_flag")
    private int forbiddenFlag;

    // 用户类型 1普通用户 2客服 3机器人
    @TableField(value = "user_type")
    private Integer userType;

    //拓展
    @TableField(value = "extra")
    private String extra;

    private int roleId;


    private int isAdmin = 0;

}
