package com.lld.im.service.group.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.Date;

@Data
public class GetGroupMessageReq extends RequestBase {

    //客户端传的messageId
    private String messageId;

    private String groupId;

    private String fromId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String search;

    private int type;

}
