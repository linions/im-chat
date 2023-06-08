package com.lld.im.common.model.message;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

@Data
public class MessageReciveAckContent extends ClientInfo {

    private String messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private int type;

    private Object mediaContent;


}
