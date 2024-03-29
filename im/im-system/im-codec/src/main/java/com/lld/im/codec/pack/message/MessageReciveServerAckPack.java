package com.lld.im.codec.pack.message;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class MessageReciveServerAckPack {

    private String messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private Boolean serverSend;

    private int type;
}
