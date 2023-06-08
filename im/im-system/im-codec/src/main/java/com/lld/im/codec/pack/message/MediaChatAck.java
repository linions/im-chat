package com.lld.im.codec.pack.message;

import lombok.Data;

@Data
public class MediaChatAck {

    private String messageId;
    private String formId;
    private String toId;
    private Boolean serverSend;

    public MediaChatAck(String messageId) {
        this.messageId = messageId;
    }
}
