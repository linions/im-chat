package com.lld.message.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.model.message.FileDto;
import com.lld.im.common.model.message.GroupChatMessageContent;
import com.lld.im.common.model.message.MessageContent;
import com.lld.message.dao.ImGroupMessageHistoryEntity;
import com.lld.message.dao.ImMessageBodyEntity;
import com.lld.message.dao.ImMessageFileEntity;
import com.lld.message.dao.ImMessageHistoryEntity;
import com.lld.message.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.message.dao.mapper.ImMessageBodyMapper;
import com.lld.message.dao.mapper.ImMessageFileMapper;
import com.lld.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.message.model.DoStoreGroupMessageDto;
import com.lld.message.model.DoStoreP2PMessageDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @description:
 * @author: linion
 * @version: 1.0
 */
@Service
public class StoreMessageService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImMessageFileMapper imMessageFileMapper;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;


    @Transactional
    public void doStoreP2PMessage(DoStoreP2PMessageDto doStoreP2PMessageDto){
        ImMessageBodyEntity imMessageBodyEntity = doStoreP2PMessageDto.getImMessageBodyEntity();
        int type = imMessageBodyEntity.getType();
        ImMessageFileEntity imMessageFileEntity = new ImMessageFileEntity();
        if( type== 2 || type == 3){
            FileDto fileContent = doStoreP2PMessageDto.getMessageContent().getFileContent();
            String suffix = fileContent.getName().substring(fileContent.getName().indexOf('.'));
            imMessageFileEntity.setName(fileContent.getName());
            imMessageFileEntity.setExtension(suffix);
            imMessageFileEntity.setOperatorId(doStoreP2PMessageDto.getMessageContent().getFromId());
            imMessageFileEntity.setSize(Integer.parseInt(fileContent.getSize()));
            imMessageFileEntity.setLocation(fileContent.getUrl());
            if(type == 2){
                imMessageFileEntity.setType(3);
            }
            if(type == 3){
                imMessageFileEntity.setType(4);
            }
            LambdaQueryWrapper<ImMessageFileEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImMessageFileEntity::getLocation,imMessageFileEntity.getLocation());
            wrapper.eq(ImMessageFileEntity::getName,imMessageFileEntity.getName());
            wrapper.eq(ImMessageFileEntity::getSize,imMessageFileEntity.getSize());
            wrapper.eq(ImMessageFileEntity::getType,imMessageFileEntity.getType());
            wrapper.eq(ImMessageFileEntity::getOperatorId,imMessageFileEntity.getOperatorId());
            ImMessageFileEntity messageFile = imMessageFileMapper.selectOne(wrapper);
            if (ObjectUtils.isNotEmpty(messageFile)){
                messageFile.setCreateTime(new Date());
                imMessageFileMapper.updateById(messageFile);
                imMessageBodyEntity.setFileId(messageFile.getFileId());
                imMessageBodyEntity.setMessageBody(messageFile.getLocation());
            }else{
                imMessageFileMapper.insert(imMessageFileEntity);
                imMessageBodyEntity.setFileId(imMessageFileEntity.getFileId());
                imMessageBodyEntity.setMessageBody(fileContent.getUrl());
            }
        }
        imMessageBodyMapper.insert(imMessageBodyEntity);
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(doStoreP2PMessageDto.getMessageContent(), imMessageBodyEntity);
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);

    }


    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent, ImMessageBodyEntity imMessageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(new Timestamp(new Date().getTime()));
        fromHistory.setMessageTime(new Timestamp(new Date().getTime()));
        fromHistory.setMessageRandom(messageContent.getMessageRandom());
        fromHistory.setSequence(messageContent.getMessageSequence());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(new Timestamp(new Date().getTime()));
        toHistory.setMessageTime(new Timestamp(new Date().getTime()));
        toHistory.setMessageRandom(messageContent.getMessageRandom());
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    @Transactional
    public void doStoreGroupMessage(DoStoreGroupMessageDto doStoreGroupMessageDto) {

        ImMessageBodyEntity imMessageBodyEntity = doStoreGroupMessageDto.getImMessageBodyEntity();
        int type = imMessageBodyEntity.getType();
        ImMessageFileEntity imMessageFileEntity = new ImMessageFileEntity();
        if( type== 2 || type == 3){
            FileDto fileContent = doStoreGroupMessageDto.getGroupChatMessageContent().getFileContent();
            String suffix = fileContent.getName().substring(fileContent.getName().indexOf('.'));
            imMessageFileEntity.setName(fileContent.getName());
            imMessageFileEntity.setExtension(suffix);
            imMessageFileEntity.setOperatorId(doStoreGroupMessageDto.getGroupChatMessageContent().getFromId());
            imMessageFileEntity.setSize(Integer.parseInt(fileContent.getSize()));
            imMessageFileEntity.setLocation(fileContent.getUrl());
            if(type == 2){
                imMessageFileEntity.setType(3);
            }
            if(type == 3){
                imMessageFileEntity.setType(4);
            }
            LambdaQueryWrapper<ImMessageFileEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImMessageFileEntity::getLocation,imMessageFileEntity.getLocation());
            wrapper.eq(ImMessageFileEntity::getName,imMessageFileEntity.getName());
            wrapper.eq(ImMessageFileEntity::getSize,imMessageFileEntity.getSize());
            wrapper.eq(ImMessageFileEntity::getType,imMessageFileEntity.getType());
            wrapper.eq(ImMessageFileEntity::getOperatorId,imMessageFileEntity.getOperatorId());
            ImMessageFileEntity messageFile = imMessageFileMapper.selectOne(wrapper);
            if (ObjectUtils.isNotEmpty(messageFile)){
                messageFile.setCreateTime(new Date());
                imMessageFileMapper.updateById(messageFile);
                imMessageBodyEntity.setFileId(messageFile.getFileId());
                imMessageBodyEntity.setMessageBody(messageFile.getLocation());
            }else{
                imMessageFileMapper.insert(imMessageFileEntity);
                imMessageBodyEntity.setFileId(imMessageFileEntity.getFileId());
                imMessageBodyEntity.setMessageBody(fileContent.getUrl());
            }
        }

        imMessageBodyMapper.insert(doStoreGroupMessageDto.getImMessageBodyEntity());
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(doStoreGroupMessageDto.getGroupChatMessageContent(),doStoreGroupMessageDto.getImMessageBodyEntity());
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);

    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent , ImMessageBodyEntity messageBodyEntity){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setMessageRandom(messageContent.getMessageRandom());
        result.setCreateTime(new Date());
        result.setDelFlag(DelFlagEnum.NORMAL.getCode());
        result.setSequence(messageContent.getMessageSequence());
        return result;
    }
}
