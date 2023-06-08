package com.lld.im.service.role.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

@Data
public class UpdateRoleReq {

    private int roleId;

    private String name;

    private int sort;

    private int extent;

    private int status;

    private String remark;

    private String operator;

}
