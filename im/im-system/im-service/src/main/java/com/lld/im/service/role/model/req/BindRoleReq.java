package com.lld.im.service.role.model.req;

import lombok.Data;

import java.util.List;

@Data
public class BindRoleReq {

    private int roleId;

    private List<String> users;

    private String operator;

}
