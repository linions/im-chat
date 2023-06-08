package com.lld.im.service.friendship.dao;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jeffreyning.mybatisplus.anno.AutoMap;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@TableName("im_friendship")
@AutoMap
public class ImFriendShipEntity {

    @TableField(value = "app_id")
    private Integer appId;

    @TableField(value = "from_id")
    private String fromId;

    @TableField(value = "to_id")
    private String toId;

    /** 备注*/
    @TableField(value = "remark")
    private String remark;

    /** 状态 1正常 2删除*/
    @TableField(value = "status")
    private Integer status;

    /** 状态 1正常 2拉黑*/
    @TableField(value = "black")
    private Integer black;

    /** 好友关系序列号*/
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @TableField(value = "friend_sequence")
    private Long friendSequence;

    /** 黑名单关系序列号*/
    @TableField(value = "black_sequence")
    private Long blackSequence;

    /** 好友来源*/
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @TableField(value = "add_source")
    private String addSource;

    @TableField(value = "extra")
    private String extra;

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



