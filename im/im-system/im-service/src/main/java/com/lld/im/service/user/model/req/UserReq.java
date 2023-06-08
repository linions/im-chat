package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class UserReq {

    private Integer appId;

    private int page;

    private int pageSize;

    private String userId;

    private String nickName;

    private String mobile;

    private String email;

    private int status = 2;

    private int forbiddenFlag = 2;

    private int roleId = -1;

    private List<Date> createTime;


}
