package com.lld.im.service.conversation.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class DeleteConversationReq extends RequestBase {

    private String conversationId;

    private String fromId;

    private String toId;

}
