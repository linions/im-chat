package com.lld.im.service.conversation.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.conversation.model.req.CreateConversationReq;
import com.lld.im.service.conversation.model.req.DeleteConversationReq;
import com.lld.im.service.conversation.model.req.UpdateConversationReq;
import com.lld.im.service.conversation.model.req.GetConversationReq;
import com.lld.im.service.conversation.service.ConversationService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("v1/conversation")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @PostMapping("/getConversationList")
    public ResponseVO getConversationList(@RequestBody @Validated GetConversationReq req)  {
        return conversationService.getConversationList(req);
    }

    //创建会话
    @PostMapping("/getConversation")
    public ResponseVO getConversation(@RequestBody @Validated CreateConversationReq req)  {
        return conversationService.getConversation(req);
    }

    @PostMapping("/deleteConversation")
    public ResponseVO deleteConversation(@RequestBody  DeleteConversationReq req)  {
        return conversationService.deleteConversation(req);
    }

    @PutMapping("/updateConversation")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq req)  {
        return conversationService.updateConversation(req);
    }

    @PostMapping("/syncConversationList")
    public ResponseVO syncFriendShipList(@RequestBody @Validated SyncReq req)  {
        return conversationService.syncConversationSet(req);
    }

}
