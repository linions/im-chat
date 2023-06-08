package com.lld.im.service.role.dao;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@TableName(value = "im_role")
public class ImRoleEntity {

    @TableId(type = IdType.AUTO)
    private int roleId;

    @TableField(value = "name")
    private String name;

    @TableField(value = "sort")
    private int sort;

    @TableField(value = "extent")
    private int extent;

    @TableField(value = "status")
    private int status;

    @TableField(value = "remark")
    private String remark;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    @TableField(value = "del_flag")
    private Integer delFlag;

    @TableField(exist = false)
    private Map<String,Object> param = new HashMap<>();
}
