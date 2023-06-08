package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotEmpty;


@Data
public class GetCertifyReq extends RequestBase {

    @TableField(value = "mobile")
    private String account;


    @TableField(value = "code")
    private String code;

}
