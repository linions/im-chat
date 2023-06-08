package com.lld.im.service.conversation.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class GetConversationReq extends RequestBase {

    private String fromId;

    private String toId;

}
