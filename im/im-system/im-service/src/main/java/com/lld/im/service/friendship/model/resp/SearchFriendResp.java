package com.lld.im.service.friendship.model.resp;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class SearchFriendResp extends RequestBase {

    // 用户id
    @NotEmpty(message = "用户id不能为空")
    @TableField(value = "user_id")
    private String userId;

    @NotEmpty(message = "appId不能为空")
    @TableField(value = "app_id")
    private Integer appId;

    // 用户名称
    @TableField(value = "nick_name")
    private String nickName;

    // 性别
    @TableField(value = "user_sex")
    private String userSex;

    //生日
    @TableField(value = "birth_day")
    private String birthDay;

    //位置
    @TableField(value = "location")
    private String location;

    // 个性签名
    @TableField(value = "self_signature")
    private String selfSignature;


    @TableField(value = "mobile")
    private String mobile;


    // 头像
    @TableField(value = "photo")
    private String photo;


    @TableField(value = "status")
    private int status;

    // 是否可以加好友 0否 1是
    @TableField(value = "disable_add_friend")
    private Integer disableAddFriend;

    // 禁言标识 1禁用 0未禁用
    @TableField(value = "silent_flag")
    private Integer silentFlag;

    // 禁用标识 1禁用 0未禁用
    @TableField(value = "forbidden_flag")
    private Integer forbiddenFlag;

    // 用户类型 1普通用户 2客服 3机器人
    @TableField(value = "user_type")
    private Integer userType;

    //拓展
    @TableField(value = "extra")
    private String extra;

    private ImFriendShipEntity friendShip;

    private Integer isFriend;

    private Integer type;

}