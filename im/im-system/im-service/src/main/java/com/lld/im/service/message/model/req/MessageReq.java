package com.lld.im.service.message.model.req;

import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class MessageReq {

    private int appId;

    private int page;

    private int pageSize;

    private String messageKey;

    private int type;

    private List<Date> createTime;


}
