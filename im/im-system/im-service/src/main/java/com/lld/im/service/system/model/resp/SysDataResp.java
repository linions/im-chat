package com.lld.im.service.system.model.resp;

import lombok.Data;


@Data
public class SysDataResp {

    private Long userCount;

    private Long onlineUser;

    private Long msgCount;

    private Long friendCount;

    private Long onlineFriend;
}
