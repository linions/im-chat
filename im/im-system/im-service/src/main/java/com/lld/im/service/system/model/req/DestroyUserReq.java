package com.lld.im.service.system.model.req;


import lombok.Data;

@Data
public class DestroyUserReq {

    private Integer appId;

    private String userId;

    private String mobile;

    private String email;

    private String code;

    private int  type;
}
