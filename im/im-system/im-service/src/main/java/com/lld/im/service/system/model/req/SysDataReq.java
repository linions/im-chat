package com.lld.im.service.system.model.req;


import lombok.Data;

@Data
public class SysDataReq {

    private Integer appId;

    private String userId;

    private int isAdmin;
}
