package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class GetGroupRequestReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

}
