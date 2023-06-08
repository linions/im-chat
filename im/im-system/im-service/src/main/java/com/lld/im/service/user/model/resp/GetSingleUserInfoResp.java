package com.lld.im.service.user.model.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.service.role.dao.ImRoleEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class GetSingleUserInfoResp {

    private String userId;

    private Integer appId;

    // 用户名称
    private String nickName;

    // 性别
    private Integer userSex;

    //生日
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDay;

    //位置
    private String location;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType;

    private String mobile;

    private String email;

    // 头像
    private String photo;

    private int status;

    // 是否可以加好友 0否 1是
    private int disableAddFriend;

    // 禁言标识 1禁用 0未禁用
    private int silentFlag;


    private int forbiddenFlag;


    private Integer userType;


    private String extra;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    private Integer delFlag;

    private int isAdmin;

    private ImRoleEntity role;

    private int roleId;

}
