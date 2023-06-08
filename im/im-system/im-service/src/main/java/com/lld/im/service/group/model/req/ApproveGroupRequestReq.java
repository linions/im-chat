package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;


@Data
public class ApproveGroupRequestReq extends RequestBase {

    private Long id;

    //1同意 2拒绝
    private Integer status;
}
