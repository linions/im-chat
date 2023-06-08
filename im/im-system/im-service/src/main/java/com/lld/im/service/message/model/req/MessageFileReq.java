package com.lld.im.service.message.model.req;

import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class MessageFileReq {

    private int appId;

    private int page;

    private int pageSize;

    private String fileName;

    private int type = 0;

    private String operator;

    private List<Date> createTime;


}
