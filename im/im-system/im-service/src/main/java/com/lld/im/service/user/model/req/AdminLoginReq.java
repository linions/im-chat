package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class AdminLoginReq extends RequestBase {

    @TableField(value = "userId")
    private String userId;

    //密码
    @TableField(value = "password")
    private String password;

}
