package com.lld.im.service.message.model.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.message.FileDto;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
public class GetMessageResp extends RequestBase {

    //客户端传的messageId
    private String messageId;

    private Integer appId;

    private String fromId;

    private String toId;

    private Long sequence;

    private int messageRandom;

    @JsonFormat(pattern = "M-dd HH:mm")
    private Timestamp messageTime;

    @JsonFormat(pattern = "M-dd HH:mm")
    private Timestamp endTime;

    private String messageBody;

    private String messageKey;

    private int type;

    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private int badgeMode;
    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private String  time;

    private Long messageLifeTime;

    private FileDto fileDto;

    private boolean isError = false;



}
