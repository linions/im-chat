package com.lld.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@TableName("im_message_history")
public class ImMessageHistoryEntity {


    private Integer appId;

    private String fromId;

    private String toId;

    private String ownerId;

    /** messageBodyId*/
    private String messageKey;
    /** 序列号*/
    private Long sequence;

    private int messageRandom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp messageTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    private Integer delFlag;
}
