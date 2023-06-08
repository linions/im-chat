package com.lld.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
@TableName("im_message_history")
public class ImMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String toId;

    @MppMultiId
    private String ownerId;

    /** messageBodyId*/
    @MppMultiId
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
