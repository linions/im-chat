package com.lld.im.common.model.message;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
public class ImMessageBody {
    private Integer appId;

    /** messageBodyId*/
    private String messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Timestamp messageTime;

    private Timestamp endTime;

    private String extra;

    private int type;

    private Integer delFlag;
}
