package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;



@Data
public class LoginReq {

    @TableField(value = "userId")
    private String userId;

    //密码
    @TableField(value = "password")
    private String password;

    @TableField(value = "mobile")
    private String mobile;

    @TableField(value = "email")
    private String email;

    @TableField(value = "code")
    private String code;

    @NotNull(message = "appId不能为空")
    private Integer appId;

    private Integer loginType;

    private int type;

    private Integer clientType;

}
