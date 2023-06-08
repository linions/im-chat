package com.lld.im.service.message.model.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class SendMessageResp {

    private String messageKey;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date messageTime;

}
