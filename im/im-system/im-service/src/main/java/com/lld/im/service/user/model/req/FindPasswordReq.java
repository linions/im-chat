package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;


@Data
public class FindPasswordReq extends RequestBase {

    // 用户id
    @NotEmpty(message = "用户id不能为空")
    private String userId;

    //密码
    @TableField(value = "mobile")
    private String mobile;

    //密码
    @TableField(value = "email")
    private String email;

    private int  type;

    //密码
    @TableField(value = "password")
    private String password;


    @TableField(value = "code")
    private String code;

}
