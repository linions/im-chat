package com.lld.im.service.message.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.Date;

@Data
public class GetMessageReq extends RequestBase {

    private String ownerId;

    private String fromId;

    private String toId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String search;

    private int type;

}
