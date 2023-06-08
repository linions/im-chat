package com.lld.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.lld.im.codec.pack.conversation.DeleteConversationPack;
import com.lld.im.codec.pack.conversation.UpdateConversationPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationErrorCode;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.ConversationEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.common.model.message.MessageReadContent;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import com.lld.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.lld.im.service.conversation.model.req.CreateConversationReq;
import com.lld.im.service.conversation.model.req.DeleteConversationReq;
import com.lld.im.service.conversation.model.req.GetConversationReq;
import com.lld.im.service.conversation.model.req.UpdateConversationReq;
import com.lld.im.service.conversation.model.resp.GetConversationResp;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.model.req.GetGroupMessageReq;
import com.lld.im.service.group.model.resp.GetGroupMessageResp;
import com.lld.im.service.group.service.GroupMessageService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.message.model.req.GetMessageReq;
import com.lld.im.service.message.model.resp.GetMessageResp;
import com.lld.im.service.message.service.MessageService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.ConversationIdGenerate;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    MessageService messageService;


    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Autowired
    GroupMessageService groupMessageService;

    public String convertConversationId(Integer type,String fromId,String toId){
        return type + "_" + fromId + "_" + toId;
    }

    public void  messageMarkRead(MessageReadContent messageReadedContent){

        String toId = messageReadedContent.getToId();
        if(messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()){
            toId = messageReadedContent.getGroupId();
        }
        String conversationId = convertConversationId(messageReadedContent.getConversationType(),messageReadedContent.getFromId(), toId);
        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id",conversationId);
        query.eq("app_id",messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if(imConversationSetEntity == null){
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent,imConversationSetEntity);
            imConversationSetEntity.setReadSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        }else{
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        }
    }

    //删除会话
    public ResponseVO deleteConversation(DeleteConversationReq req){

        //置顶 有免打扰
        LambdaQueryWrapper<ImConversationSetEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(req.getConversationId()),ImConversationSetEntity::getConversationId,req.getConversationId());
        queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImConversationSetEntity::getAppId,req.getAppId());
        queryWrapper.eq(StringUtils.isNotEmpty(req.getFromId()),ImConversationSetEntity::getFromId,req.getFromId());
        queryWrapper.eq(StringUtils.isNotEmpty(req.getToId()),ImConversationSetEntity::getToId,req.getToId());
        imConversationSetMapper.delete(queryWrapper);

        return ResponseVO.successResponse();
    }

    //更新会话 置顶or免打扰
    public ResponseVO updateConversation(UpdateConversationReq req){

        if(req.getIsTop() == null && req.getIsMute() == null){
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id",req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if(imConversationSetEntity != null){
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);

            if(req.getIsTop() != null){
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if(req.getIsMute() != null){
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Conversation, seq);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_UPDATE, pack,new ClientInfo(req.getAppId(),req.getClientType(), req.getImei()));
        }
        return ResponseVO.successResponse();
    }


//    同步会话
    public ResponseVO syncConversationSet(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getOperator());
        queryWrapper.gt("sequence",req.getLastSequence());
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper.selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(list)){
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperator());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);

    }

//    获取会话列表
    public ResponseVO getConversationList(GetConversationReq req) {
        List<GetConversationResp> resps = new ArrayList<>();

        LambdaQueryWrapper<ImConversationSetEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImConversationSetEntity::getAppId, req.getAppId());
        wrapper.eq(StringUtils.isNotEmpty(req.getFromId()),ImConversationSetEntity::getFromId, req.getFromId());
        wrapper.eq(StringUtils.isNotEmpty(req.getToId()),ImConversationSetEntity::getToId, req.getToId());
        wrapper.orderByDesc(ImConversationSetEntity::getIsTop);
        wrapper.orderByDesc(ImConversationSetEntity::getSequence);
        List<ImConversationSetEntity> imConversationSetEntities = imConversationSetMapper.selectList(wrapper);



        for (ImConversationSetEntity e : imConversationSetEntities) {//最新聊天信息
            GetConversationResp getConversationResp = new GetConversationResp();
            BeanUtils.copyProperties(e,getConversationResp);

            if (ConversationTypeEnum.P2P.getCode() == e.getConversationType()){
                GetMessageReq getMessageReq = new GetMessageReq();
                getMessageReq.setOwnerId(e.getFromId());
                getMessageReq.setFromId(e.getFromId());
                getMessageReq.setToId(e.getToId());
                getMessageReq.setAppId(e.getAppId());
                ResponseVO<GetMessageResp> updatedMessage = messageService.getUpdatedMessage(getMessageReq);
                getConversationResp.setMessage(updatedMessage.getData());

                if(ObjectUtils.isNotEmpty(updatedMessage.getData())){
                    String key =  req.getAppId() + ":" + Constants.SeqConstants.Message+ ":" + ConversationIdGenerate.generateP2PId(getMessageReq.getFromId(),getMessageReq.getToId());
                    long valueSeq = redisSeq.getValueSeq(key);

                    String hashKey = getMessageReq.getAppId() + ":" + Constants.RedisConstants.SeqPrefix + ":" + e.getFromId();
                    key = Constants.SeqConstants.Message + ":" + e.getToId() ;
                    long hashSeq = redisSeq.getHashSeq(hashKey, key);
                    if(hashSeq < valueSeq){
                        getConversationResp.setIsRead(0);
                    }else {
                        getConversationResp.setIsRead(1);
                    }

                }


                ResponseVO singleUserInfo = imUserService.getSingleUserInfo(e.getToId(), e.getAppId());
                getConversationResp.setDataInfo(singleUserInfo.getData());

                LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
                query.eq(ObjectUtils.isNotEmpty(e.getFromId()), ImFriendShipEntity::getFromId, e.getFromId());
                query.eq(ObjectUtils.isNotEmpty(e.getToId()), ImFriendShipEntity::getToId, e.getToId());
                ImFriendShipEntity formFriendShip = imFriendShipMapper.selectOne(query);

                LambdaQueryWrapper<ImFriendShipEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ObjectUtils.isNotEmpty(e.getFromId()), ImFriendShipEntity::getToId, e.getFromId());
                queryWrapper.eq(ObjectUtils.isNotEmpty(e.getToId()), ImFriendShipEntity::getFromId, e.getToId());
                ImFriendShipEntity toFriendShip = imFriendShipMapper.selectOne(query);
                if(toFriendShip.getStatus() != 1 || formFriendShip.getStatus() != 1){
                    formFriendShip.setStatus(2);
                }
                getConversationResp.setFriendShip(formFriendShip);

            }else if(ConversationTypeEnum.GROUP.getCode() == e.getConversationType()) {
                GetGroupMessageReq getMessageReq = new GetGroupMessageReq();
                getMessageReq.setGroupId(e.getToId());
                getMessageReq.setAppId(e.getAppId());
                ResponseVO<GetGroupMessageResp> updatedMessage = groupMessageService.getUpdatedMessage(getMessageReq);
                getConversationResp.setMessage(updatedMessage.getData());

                if (ObjectUtils.isNotEmpty(updatedMessage.getData())) {
                    String key = getMessageReq.getAppId() + ":" + Constants.SeqConstants.GroupMessage + ":" + getMessageReq.getGroupId();
                    long valueSeq = redisSeq.getValueSeq(key);
//                    Long sequence = updatedMessage.getData().getSequence();

                    String hashKey = getMessageReq.getAppId() + ":" +Constants.RedisConstants.SeqPrefix + ":" + e.getFromId();
                    key = Constants.SeqConstants.GroupMessage + ":" + e.getToId();
//                    System.out.println("valueSeq = " + hashKey);
//                    System.out.println("valueSeq = " + key);
                    long hashSeq = redisSeq.getHashSeq(hashKey, key);
//                    System.out.println("valueSeq = " + valueSeq);
//                    System.out.println("hashSeq = " + hashSeq);

                    if (hashSeq < valueSeq) {
                        getConversationResp.setIsRead(0);
                    } else {
                        getConversationResp.setIsRead(1);
                    }
                }
                ResponseVO<ImGroupEntity> group = imGroupService.getGroup(e.getToId(), e.getAppId());
                if (group.isOk()) {
                    getConversationResp.setDataInfo(group.getData());
                }
            }

            resps.add(getConversationResp);
        }
        return ResponseVO.successResponse(resps);
    }

//    获取单个聊天会话信息
    public ResponseVO getConversation(CreateConversationReq req) {

        String toId = req.getToId();
        if(req.getConversationType() == ConversationTypeEnum.GROUP.getCode()){
            toId = req.getGroupId();
        }
        String conversationId = convertConversationId(req.getConversationType(),req.getFromId(), toId);
        LambdaQueryWrapper<ImConversationSetEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImConversationSetEntity::getConversationId,conversationId);
        ImConversationSetEntity imConversationSet = imConversationSetMapper.selectOne(query);
        if (ObjectUtils.isEmpty(imConversationSet)){
            ImConversationSetEntity imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);
            BeanUtils.copyProperties(req,imConversationSetEntity);
            imConversationSetEntity.setConversationId(conversationId);
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            int insert = imConversationSetMapper.insert(imConversationSetEntity);
            if (insert == 0) {
                return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_INSERT_ERROR);
            }
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Conversation, seq);
        }
        GetConversationReq getConversationReq = new GetConversationReq();
        getConversationReq.setAppId(req.getAppId());
        getConversationReq.setFromId(req.getFromId());
        getConversationReq.setToId(toId);

        return this.getConversationList(getConversationReq);


    }
}
