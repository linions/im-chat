package com.lld.im.service.role.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

@Data
public class GetRoleReq extends RequestBase {

    private int page;

    private int pageSize;

    private String name;

    private int status = 2;
}
