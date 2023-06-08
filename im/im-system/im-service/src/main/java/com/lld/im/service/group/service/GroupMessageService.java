package com.lld.im.service.group.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.FileDto;
import com.lld.im.common.model.message.GroupChatMessageContent;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.im.service.group.model.req.GetGroupMessageReq;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.model.req.SendGroupMessageReq;
import com.lld.im.service.group.model.resp.GetGroupMessageResp;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageFileEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageFileMapper;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.message.service.CheckSendMessageService;
import com.lld.im.service.message.service.MessageStoreService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lld.im.common.utils.Base64Util.deCode;


@Service
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    private ImMessageFileMapper imMessageFileMapper;


    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImUserDataMapper imUserDataMapper;


    @Autowired
    WriteUserSeq writeUserSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-group-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(GroupChatMessageContent messageContent){
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友

        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), GroupChatMessageContent.class);
        if(messageFromMessageIdCache != null){
            threadPoolExecutor.execute(() ->{
                //1.回ack成功给自己
                ack(messageContent,ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageContent,messageContent);
                //3.发消息给对方在线端
                dispatchMessage(messageContent);
            });
        }

        long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.GroupMessage + ":" + messageContent.getGroupId());
        messageContent.setMessageSequence(seq);
        writeUserSeq.writeUserSeq(messageContent.getAppId(),messageContent.getFromId(),Constants.SeqConstants.GroupMessage+":"+messageContent.getGroupId(),seq);
        threadPoolExecutor.execute(() ->{
            messageStoreService.storeGroupMessage(messageContent);
            //  插入离线消息
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(), messageContent.getAppId());
            messageContent.setMemberId(groupMemberId);

//            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
//            BeanUtils.copyProperties(messageContent,offlineMessageContent);
//            offlineMessageContent.setToId(messageContent.getGroupId());
//            messageStoreService.storeGroupOfflineMessage(offlineMessageContent,groupMemberId);

            //1.回ack成功给自己
            ack(messageContent,ResponseVO.successResponse());
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);

            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(),messageContent);
        });
    }

    private void dispatchMessage(GroupChatMessageContent messageContent){
        for (String memberId : messageContent.getMemberId()) {
            if(!memberId.equals(messageContent.getFromId())){
                messageProducer.sendToUser(memberId,GroupEventCommand.MSG_GROUP, messageContent,messageContent.getAppId());
            }
        }
    }

    private void ack(MessageContent messageContent,ResponseVO responseVO){

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //發消息
        messageProducer.sendToUser(messageContent.getFromId(),GroupEventCommand.GROUP_MSG_ACK,responseVO,messageContent);
    }

    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                GroupEventCommand.MSG_GROUP,messageContent,messageContent);
    }

    public SendMessageResp send(SendGroupMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req,message);
        ResponseVO<List<GroupMemberDto>> groupMember = imGroupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
        if (groupMember.isOk() && groupMember.getData().size()>0){
            List<String> memberIds = new ArrayList<>();
            List<GroupMemberDto> data = groupMember.getData();
            data.forEach(e->{
                memberIds.add(e.getMemberId());
            });
            message.setMemberId(memberIds);
        }else {
            message.setMemberId(null);
        }


        messageStoreService.storeGroupMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(new Date());
        //2.发消息给同步在线端
        syncToSender(message,message);
        //3.发消息给对方在线端
        dispatchMessage(message);

        return sendMessageResp;

    }

    //    获取最新的聊天记录
    public ResponseVO<GetGroupMessageResp> getUpdatedMessage(GetGroupMessageReq req) {
        GetGroupMessageResp getGroupMessageResp = new GetGroupMessageResp();
//        ImGroupMessageHistoryEntity updatedMessage = imGroupMessageHistoryMapper.getUpdatedMessage(req);
        ImGroupMessageHistoryEntity updatedMessage = imGroupMessageHistoryMapper.getUpdatedMessageByTime(req);
        if (ObjectUtils.isEmpty(updatedMessage)){
            return ResponseVO.successResponse(null);
        }
        BeanUtils.copyProperties(updatedMessage,getGroupMessageResp);
        //封装信息体
        LambdaQueryWrapper<ImMessageBodyEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(updatedMessage.getAppId()),ImMessageBodyEntity::getAppId, updatedMessage.getAppId());
        query.eq(ObjectUtils.isNotEmpty(updatedMessage.getMessageKey()),ImMessageBodyEntity::getMessageKey,updatedMessage.getMessageKey());
        ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(query);

        LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtils.isNotEmpty(messageBody.getAppId()),ImUserDataEntity::getAppId, messageBody.getAppId());
        wrapper.eq(ObjectUtils.isNotEmpty(updatedMessage.getFromId()),ImUserDataEntity::getUserId,updatedMessage.getFromId());
        ImUserDataEntity imUserDataEntity = imUserDataMapper.selectOne(wrapper);
        if(messageBody.getType() != 2 && messageBody.getType() != 3){
            String message = deCode(messageBody.getMessageBody(), messageBody.getSecurityKey());
            getGroupMessageResp.setMessageBody(message);
        }else {
            getGroupMessageResp.setMessageBody(messageBody.getMessageBody());
        }
        getGroupMessageResp.setUserData(imUserDataEntity);
        getGroupMessageResp.setType(messageBody.getType());
        return ResponseVO.successResponse(getGroupMessageResp);
    }

    /**
     * 计算文件大小
     * @return 1GB
     * */
    public String getFileSize(float size) {
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        try {
            // 格式化小数
            DecimalFormat df = new DecimalFormat("0.00");
            String resultSize = "";
            if (size / GB >= 1) {
                //如果当前Byte的值大于等于1GB
                resultSize = df.format(size / (float) GB) + "GB";
            } else if (size / MB >= 1) {
                //如果当前Byte的值大于等于1MB
                resultSize = df.format(size / (float) MB) + "MB";
            } else if (size / KB >= 1) {
                //如果当前Byte的值大于等于1KB
                resultSize = df.format(size / (float) KB) + "KB";
            } else {
                resultSize = size + "B";
            }
            return resultSize;
        } catch (Exception e) {
            return null;
        }
    }

    //获取聊天信息记录
    public ResponseVO getChatMessage(GetGroupMessageReq req) {
        List<GetGroupMessageResp> respList = new ArrayList<>();

        List<ImGroupMessageHistoryEntity> imMessageHistoryEntities = imGroupMessageHistoryMapper.selectMessage(req);
        imMessageHistoryEntities.forEach(e->{
            GetGroupMessageResp getGroupMessageResp = new GetGroupMessageResp();
            BeanUtils.copyProperties(e,getGroupMessageResp);
            //封装信息体
            LambdaQueryWrapper<ImMessageBodyEntity> query = new LambdaQueryWrapper<>();
            query.eq(ObjectUtils.isNotEmpty(e.getAppId()),ImMessageBodyEntity::getAppId, e.getAppId());
            query.eq(ObjectUtils.isNotEmpty(e.getMessageKey()),ImMessageBodyEntity::getMessageKey,e.getMessageKey());

            ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(query);
            if(messageBody.getType() == 2 || messageBody.getType() == 3){
                ImMessageFileEntity imMessageFileEntity = imMessageFileMapper.selectById(messageBody.getFileId());
                FileDto fileDto = new FileDto();
                fileDto.setName(imMessageFileEntity.getName());
                fileDto.setUrl(imMessageFileEntity.getLocation());
                fileDto.setSize(getFileSize(imMessageFileEntity.getSize()));
                fileDto.setFileType(imMessageFileEntity.getType());
                getGroupMessageResp.setFileDto(fileDto);
            }

            if(messageBody.getType() != 2 && messageBody.getType() != 3){
                String message = deCode(messageBody.getMessageBody(), messageBody.getSecurityKey());
                getGroupMessageResp.setMessageBody(message);
            }else {
                getGroupMessageResp.setMessageBody(messageBody.getMessageBody());
            }
            getGroupMessageResp.setType(messageBody.getType());
            getGroupMessageResp.setEndTime(messageBody.getEndTime());



            LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ObjectUtils.isNotEmpty(e.getAppId()),ImUserDataEntity::getAppId, e.getAppId());
            wrapper.eq(ObjectUtils.isNotEmpty(e.getFromId()),ImUserDataEntity::getUserId,e.getFromId());
            ImUserDataEntity imUserDataEntity = imUserDataMapper.selectOne(wrapper);
            getGroupMessageResp.setUserData(imUserDataEntity);

            if(StringUtils.isNotBlank(req.getSearch()) && req.getType() != 0){
                if(getGroupMessageResp.getMessageBody().contains(req.getSearch()) && getGroupMessageResp.getType() == req.getType() ){
                    respList.add(getGroupMessageResp);
                }
            }else if(StringUtils.isBlank(req.getSearch()) && req.getType() != 0){
                if(getGroupMessageResp.getType() == req.getType() ){
                    respList.add(getGroupMessageResp);
                }
            }else {
                respList.add(getGroupMessageResp);
            }
        });
        String key = req.getAppId() + ":" + Constants.SeqConstants.GroupMessage + ":" + req.getGroupId();
        long valueSeq = redisSeq.getValueSeq(key);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperator(),Constants.SeqConstants.GroupMessage+":"+ req.getGroupId(),valueSeq);
        return ResponseVO.successResponse(respList);
    }

}
