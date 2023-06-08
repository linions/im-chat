package com.lld.im.service.system.dao;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@TableName(value = "im_sys_param")
public class ImSysParamEntity {

    @TableId(type = IdType.AUTO)
    private int  id;

    private String name;

    private String paramKey;

    private String paramValue;

    private int isSys;

    private String remark;


    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;


    @TableField(value = "del_flag")
    private Integer delFlag;
}
