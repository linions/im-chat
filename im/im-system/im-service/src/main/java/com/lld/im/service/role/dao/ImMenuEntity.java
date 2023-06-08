package com.lld.im.service.role.dao;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "im_menu")
public class ImMenuEntity {

    @TableId(value = "menu_id",type = IdType.AUTO)
    private int menuId;

    @TableField(value = "parent_id")
    private int parentId;

    @TableField(value = "menu_key")
    private String menuKey;

    @TableField(value = "name")
    private String name;

    @TableField(value = "status")
    private int status;

    @TableField(value = "logo")
    private String logo;

    @TableField(value = "sort")
    private int sort;

    @TableField(value = "route")
    private String route;

    @TableField(value = "component")
    private String component;

    @TableField(value = "type")
    private int type;

    @TableField(value = "limit_char")
    private String limitChar;

    @TableField(value = "is_show")
    private int isShow;

    @TableField(value = "is_connect")
    private int isConnect;

    @TableField(value = "is_cache")
    private int isCache;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
