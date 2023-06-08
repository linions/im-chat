package com.lld.im.common.model.message;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
public class OfflineMessageContent {

    private Integer appId;

    /** messageBodyId*/
    private String messageKey;

    /** messageBody*/
    private String messageBody;

    private Timestamp messageTime;

    private String extra;

    private Integer delFlag;

    private String fromId;

    private String toId;

    private int type;

    /** 序列号*/
    private Long messageSequence;

    private String messageRandom;

    private Integer conversationType;

    private String conversationId;

}
