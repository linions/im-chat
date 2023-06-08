package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class LogoutReq {

    @NotNull(message = "userId不能为空")
    private String userId;


    private Integer appId;


    private Integer clientType;

}
