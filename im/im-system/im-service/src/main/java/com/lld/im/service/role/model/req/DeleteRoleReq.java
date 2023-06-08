package com.lld.im.service.role.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

@Data
public class DeleteRoleReq extends RequestBase {

    private int roleId;

    private int isAdmin;
}
