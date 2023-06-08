package com.lld.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@TableName("im_group_message_history")
public class ImGroupMessageHistoryEntity {


    private Integer appId;

    private String fromId;

    private String groupId;

    /** messageBodyId*/
    @TableId
    private String messageKey;
    /** 序列号*/
    private Long sequence;

    private String messageRandom;

    @JsonFormat(pattern = "M-dd HH:mm")
    private Timestamp messageTime;

    @JsonFormat(pattern = "M-dd HH:mm")
    private Timestamp createTime;

    private Integer delFlag;


}
