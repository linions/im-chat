package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.message.MessageReciveServerAckPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.MediaEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.ImMessageBody;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.MessageReciveAckContent;
import com.lld.im.common.model.message.OfflineMessageContent;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.ConversationIdGenerate;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MediaChatService {

    private static Logger logger = LoggerFactory.getLogger(MediaChatService.class);

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;


    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    //离线
    //存储介质
    //1.mysql
    //2.redis
    //怎么存？
    //list
    //set
    //zet messageKey message


    //历史消息

    //发送方客户端时间
    //messageKey
    //redis 1 2 3
    public void process(MessageContent messageContent){

        logger.info("消息开始处理：{}",messageContent.getMessageId());

        //回调
        ResponseVO responseVO = ResponseVO.successResponse();
        if(appConfig.isSendMessageBeforeCallback()){
            responseVO = callbackService.beforeCallback(messageContent.getAppId(), Constants.CallbackCommand.SendMediaBefore, JSONObject.toJSONString(messageContent));
        }

        if(!responseVO.isOk()){
            ack(messageContent,responseVO);
            return;
        }
        //appId + Seq + (from + to) groupId
        long seq = redisSeq.doGetSeq(messageContent.getAppId() +":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(messageContent.getFromId(),messageContent.getToId()));
        messageContent.setMessageSequence(seq);
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
        threadPoolExecutor.execute(() ->{
            //插入数据
            //1.回ack成功给自己
//            ack(messageContent,ResponseVO.successResponse());
            //2.发消息给同步在线端
//            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(),messageContent);
            if(clientInfos.isEmpty()){
                //发送接收确认给发送方，要带上是服务端发送的标识
                ack(messageContent,ResponseVO.errorResponse());
            }else{
                send(messageContent);
            }

            if(appConfig.isMediaCallAfterCallback()){
                callbackService.callback(messageContent.getAppId(),Constants.CallbackCommand.SendMediaAfter, JSONObject.toJSONString(messageContent));
            }

            logger.info("消息处理完成：{}",messageContent.getMessageId());
        });
    }

    private List<ClientInfo> dispatchMessage(MessageContent messageContent){
        List<ClientInfo> clientInfos;
        if (messageContent.getType() == 1){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.CALL_VOICE, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 2){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.CALL_VIDEO, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 3){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.ACCEPT_CALL, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 4){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.REJECT_CALL, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 5){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.HANG_UP, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 6){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.CANCEL_CALL, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 7){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.TIMEOUT_CALL, messageContent, messageContent.getAppId());
        }else if (messageContent.getType() == 8){
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.TRANSMIT_OFFER, messageContent, messageContent.getAppId());
        }else{
            clientInfos = messageProducer.sendToUser(messageContent.getToId(), MediaEventCommand.TRANSMIT_ANSWER, messageContent, messageContent.getAppId());
        }
        return clientInfos;
    }

    private void ack(MessageContent messageContent,ResponseVO responseVO){
        logger.info("msg ack,msgId = {},checkResult = {}",messageContent.getMessageId(),responseVO.getCode());
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setType(messageContent.getType());
        pack.setMessageSequence(messageContent.getMessageSequence());
        if(responseVO.getCode() == 200){
            pack.setServerSend(true);
        }else {
            pack.setServerSend(false);
        }
        //發消息
        messageProducer.sendToUser(messageContent.getFromId(), MediaEventCommand.ACK, pack,messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        if (messageContent.getType() == 1){
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MediaEventCommand.CALL_VOICE,messageContent,messageContent);
        }else if (messageContent.getType() == 2){
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MediaEventCommand.CALL_VIDEO,messageContent,messageContent);
        }else if (messageContent.getType() == 3){
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MediaEventCommand.ACCEPT_CALL,messageContent,messageContent);
        }else if (messageContent.getType() == 4){
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MediaEventCommand.REJECT_CALL,messageContent,messageContent);
        }else if (messageContent.getType() == 5){
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MediaEventCommand.HANG_UP,messageContent,messageContent);
        }else{
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MediaEventCommand.CANCEL_CALL,messageContent,messageContent);
        }
    }

    public MessageContent send(MessageContent message) {

        long seq = redisSeq.doGetSeq(message.getAppId() + ":" + Constants.SeqConstants.Message+ ":" + ConversationIdGenerate.generateP2PId(message.getFromId(),message.getToId()));
        ImMessageBody imMessageBody = messageStoreService.mediaMessageBody(message);
        logger.info("消息开始处理：{}",imMessageBody.getMessageKey());
        message.setMessageKey(imMessageBody.getMessageKey());
        message.setMessageSequence(seq);
        messageStoreService.storeMediaMessage(message,imMessageBody);
            //插入数据
            ack(message,ResponseVO.successResponse());
            //2.发消息给同步在线端
            syncToSender(message,message);
            //3.发消息给对方在线端
            dispatchMessage(message);
        return message;
    }

    public void acceptCall(MessageReciveAckContent messageReciveAckContent) {
            MessageContent messageContent = new MessageContent();

            BeanUtils.copyProperties(messageReciveAckContent,messageContent);
            logger.info(" messageContent： {} ",messageContent);

            ack(messageContent,ResponseVO.successResponse());
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);
    }

    public void handle(MessageReciveAckContent messageReciveAckContent) {
        MessageContent messageContent = new MessageContent();
        BeanUtils.copyProperties(messageReciveAckContent,messageContent);
        boolean b = messageStoreService.handleCall(messageReciveAckContent);
        if (b){
            ack(messageContent,ResponseVO.successResponse());
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);
        }else{
            ack(messageContent,ResponseVO.errorResponse());
        }
    }


    public void handleAck(MessageReciveAckContent messageReciveAckContent) {
        MessageContent messageContent = new MessageContent();
        BeanUtils.copyProperties(messageReciveAckContent,messageContent);
        logger.info(" messageContent： {} ",messageContent);
        //3.发消息给对方在线端
        dispatchMessage(messageContent);
        ack(messageContent,ResponseVO.successResponse());
    }
}
