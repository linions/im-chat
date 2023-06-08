package com.lld.im.common.model.message;

import com.alibaba.fastjson.annotation.JSONField;
import com.lld.im.common.model.ClientInfo;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.utils.DateUtils;

import java.sql.Timestamp;
import java.util.Date;


@Data
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;

    private Timestamp messageTime;

    private int messageRandom;

    private String extra;

    private int type;

    private String messageKey;

    private Long messageSequence;

    private Object mediaContent;

    private FileDto FileContent;

}
