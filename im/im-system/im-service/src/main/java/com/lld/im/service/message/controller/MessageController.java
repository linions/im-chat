package com.lld.im.service.message.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.command.MediaEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.message.CheckSendMessageReq;
import com.lld.im.service.message.model.req.*;
import com.lld.im.service.message.service.CheckSendMessageService;
import com.lld.im.service.message.service.MessageService;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import com.lld.im.service.user.model.req.DownLoadFileReq;
import com.lld.im.service.user.model.req.UserReq;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;


@RestController
@CrossOrigin
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @Autowired
    MessageService messageService;

    @PostMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req)  {
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @PostMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        if (MessageCommand.MSG_P2P.getCommand() == req.getCommand() || MediaEventCommand.CALL_VIDEO.getCommand() == req.getCommand() || MediaEventCommand.CALL_VOICE.getCommand() == req.getCommand()){
            return checkSendMessageService.checkFriendShip(req.getFromId(),req.getToId(),req.getAppId());
        }else{
            return checkSendMessageService.checkGroupMessage(req.getFromId(),req.getToId(),req.getAppId());
        }
    }

    @PostMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody @Validated SyncReq req)  {
        return messageSyncService.syncOfflineMessage(req);
    }

//    获取聊天记录
    @PostMapping("/getChatMessage")
    public ResponseVO getChatMessage(@RequestBody @Validated GetMessageReq req)  {
        return messageService.getChatMessage(req);
    }

    @PostMapping("/uploadMsgFile")
    public ResponseVO uploadMsgFile(@RequestParam("file") MultipartFile uploadFile) throws IOException {
        return messageService.uploadMsgFile(uploadFile);
    }

    @PostMapping("/downLoadFile")
    public ResponseVO downLoadFile(HttpServletRequest request,@RequestBody DownLoadFileReq req) throws IOException {
        return messageService.downLoadFile(request,req);
    }

    @PostMapping("/getMessageByPage")
    public ResponseVO getMessageByPage(@RequestBody MessageReq req){
        return messageService.getMessageByPage(req);
    }

    //    折线图数据
    @GetMapping("/getDataOfWeek/{appId}")
    public ResponseVO getDataOfWeek(@PathVariable Integer appId) {
        return messageService.getDataOfWeek(appId);
    }


    @DeleteMapping("/delete/{appId}/{messageKey}/{operator}")
    public ResponseVO deleteMessageByKey(HttpServletRequest request, @PathVariable Integer appId, @PathVariable String messageKey,@PathVariable String operator) {
        return messageService.deleteMessageByKey(request,appId,messageKey,operator);
    }

//    获取文件
    @PostMapping("/getMessageFileByPage")
    public ResponseVO getMessageFileByPage(@RequestBody MessageFileReq req){
        return messageService.getMessageFileByPage(req);
    }

    @DeleteMapping("/deleteMsgFile/{fileId}/{operator}")
    public ResponseVO deleteMessageFileById(HttpServletRequest request,@PathVariable Integer fileId,@PathVariable String operator) {
        return messageService.deleteMessageFileById(request,fileId,operator);
    }

}
