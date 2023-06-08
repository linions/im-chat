package com.lld.im.service.conversation.model.resp;

import com.lld.im.common.model.RequestBase;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.message.model.resp.GetMessageResp;
import com.lld.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class GetConversationResp extends RequestBase {

    //会话id 0_fromId_toId
    private String conversationId;

    //会话类型
    private Integer conversationType;

    private String fromId;

    private String toId;

    private int isMute;

    private int isTop;

    private Long sequence;

    private Long readSequence;

    private Integer appId;

    private ImFriendShipEntity friendShip;

    private Object dataInfo;

    private Object message;

    private int isRead = 1;

}
