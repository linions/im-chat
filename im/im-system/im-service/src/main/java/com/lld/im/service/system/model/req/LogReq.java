package com.lld.im.service.system.model.req;


import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class LogReq {

    private Integer appId;

    private String userId;

    private List<Date> time;
}
