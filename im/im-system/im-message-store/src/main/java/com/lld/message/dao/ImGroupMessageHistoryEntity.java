package com.lld.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
@TableName("im_group_message_history")
public class ImGroupMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String groupId;

    /** messageBodyId*/
    private String messageKey;
    /** 序列号*/
    private Long sequence;

    private int messageRandom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date messageTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer delFlag;


}
