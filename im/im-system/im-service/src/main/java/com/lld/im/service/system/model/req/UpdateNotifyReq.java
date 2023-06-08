package com.lld.im.service.system.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;


@Data
public class UpdateNotifyReq extends RequestBase {

    private Integer id;

    private String title;

    private String content;

    private String toId;

    private int type;

}
