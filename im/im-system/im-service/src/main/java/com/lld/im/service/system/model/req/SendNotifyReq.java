package com.lld.im.service.system.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;


@Data
public class SendNotifyReq extends RequestBase {

    private String fromId;

    private String toId;

    private String title;

    private String content;

    private int type;

    private int isAdmin = 0;

}
