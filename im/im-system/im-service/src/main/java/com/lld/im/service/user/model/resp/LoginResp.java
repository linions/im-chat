package com.lld.im.service.user.model.resp;

import com.lld.im.common.route.RouteInfo;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class LoginResp {

    private RouteInfo routeInfo;

    @NotNull
    private String  userId;

    private Integer appId;

    private String userSign;

    private String mobile;

    private String email;

    private String nickName;

}
