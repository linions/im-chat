package com.lld.im.service.user.dao;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Data
@TableName("im_user_data")
public class ImUserDataEntity {

    // 用户id
    @NotEmpty(message = "用户id不能为空")
    @TableField(value = "user_id")
    @TableId
    private String userId;

    @NotEmpty(message = "appId不能为空")
    @TableField(value = "app_id")
    private Integer appId;

    // 用户名称
    @TableField(value = "nick_name")
    private String nickName;

    // 性别
    @TableField(value = "user_sex")
    private Integer userSex;

    //生日
    @TableField(value = "birth_day")
    @JsonFormat(pattern = "yyyy-MM-dd")
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

    @TableField(value = "mobile")
    private String mobile;

    @TableField(value = "email")
    private String email;

    //密码
    @TableField(value = "password")
    private String password;

    // 头像
    @TableField(value = "photo")
    private String photo;

    @TableField(value = "status")
    private int status;
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

    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time")
    private Timestamp updateTime;


    @TableField(value = "del_flag")
    private Integer delFlag;

    @TableField(value = "role_id")
    private int roleId;

    @TableField(exist = false)
    private Map<String,Object> param = new HashMap<>();

}
