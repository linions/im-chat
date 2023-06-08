package com.lld.im.service.role.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.Date;

@Data
public class RoleReq {

    private String name;

    private int sort;

    private int range;

    private int extent;

    private String remark;

    private String operator;

}
