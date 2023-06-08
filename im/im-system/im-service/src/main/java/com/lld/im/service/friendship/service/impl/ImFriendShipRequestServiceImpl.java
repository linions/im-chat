package com.lld.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.friendship.ApproveFriendRequestPack;
import com.lld.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ApproverFriendRequestStatusEnum;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Service
public class ImFriendShipRequestServiceImpl extends ServiceImpl<ImFriendShipRequestMapper, ImFriendShipRequestEntity> implements ImFriendShipRequestService {

    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;

    @Autowired
    ImFriendService imFriendShipService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;


    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public ResponseVO getFriendRequest(GetFriendShipRequestReq req) {

        LambdaQueryWrapper<ImFriendShipRequestEntity> query = new LambdaQueryWrapper();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImFriendShipRequestEntity::getAppId, req.getAppId());
        query.and(wrapper->wrapper.eq(StringUtils.isNotBlank(req.getFromId()),ImFriendShipRequestEntity::getFromId, req.getFromId())
                .or().eq(StringUtils.isNotBlank(req.getFromId()),ImFriendShipRequestEntity::getToId, req.getFromId()));
        query.eq(ImFriendShipRequestEntity::getDelFlag, DelFlagEnum.NORMAL);
        query.orderByDesc(ImFriendShipRequestEntity::getUpdateTime);

        List<ImFriendShipRequestEntity> list = imFriendShipRequestMapper.selectList(query);
        list.forEach(e->{
            ResponseVO userInfo ;
            if(e.getToId().equals(req.getFromId())) {
                userInfo = imUserService.getSingleUserInfo(e.getFromId(), req.getAppId());
            }else {
                userInfo = imUserService.getSingleUserInfo(e.getToId(), req.getAppId());
            }
            e.getParam().put("userInfo",userInfo.getData());
        });

        return ResponseVO.successResponse(list);
    }


    //A + B
    @Override
    public ResponseVO addFriendshipRequest(String fromId, FriendDto dto, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId);
        queryWrapper.eq("from_id",fromId);
        queryWrapper.eq("to_id",dto.getToId());
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(queryWrapper);

        long seq = redisSeq.doGetSeq(appId+":"+ Constants.SeqConstants.FriendshipRequest);

        if(ObjectUtils.isEmpty(request)){
            request = new ImFriendShipRequestEntity();
            request.setAddSource(dto.getAddSource());
            request.setAddWording(dto.getAddWording());
            request.setSequence(seq);
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            imFriendShipRequestMapper.insert(request);

        }else {
            //修改记录内容 和更新时间
            if(StringUtils.isNotBlank(dto.getAddSource())){
                request.setAddSource(dto.getAddSource());
            }
            if(StringUtils.isNotBlank(dto.getRemark())){
                request.setRemark(dto.getRemark());
            }
            if(StringUtils.isNotBlank(dto.getAddWording())){
                request.setAddWording(dto.getAddWording());
            }
            request.setSequence(seq);
            request.setApproveStatus(0);
            request.setReadStatus(0);
            request.setDelFlag(0);
            imFriendShipRequestMapper.updateById(request);
        }

        writeUserSeq.writeUserSeq(appId,dto.getToId(),Constants.SeqConstants.FriendshipRequest,seq);

        //发送好友申请的tcp给接收方
        messageProducer.sendToUser(dto.getToId(),null, "", FriendshipEventCommand.FRIEND_REQUEST, request, appId);
        return ResponseVO.successResponse("好友申请已发送");
    }

    @Override
    @Transactional
    public ResponseVO approveFriendRequest(ApproveFriendRequestReq req) {

        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        if(imFriendShipRequestEntity == null){
            storeLog(req.getAppId(),req.getOperator(),0,"审批好友申请");
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if(!req.getOperator().equals(imFriendShipRequestEntity.getToId())){
            //只能审批发给自己的好友请求
            storeLog(req.getAppId(),req.getOperator(),0,"审批好友申请");
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }

        long seq = redisSeq.doGetSeq(req.getAppId()+":"+ Constants.SeqConstants.FriendshipRequest);

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setSequence(seq);
        update.setId(req.getId());
        imFriendShipRequestMapper.updateById(update);

        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperator(),Constants.SeqConstants.FriendshipRequest,seq);

        if(ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()){
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());

            ResponseVO responseVO = imFriendShipService.doAddFriend(req,imFriendShipRequestEntity.getFromId(), dto,req.getAppId());
            if(!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()){
                storeLog(req.getAppId(),req.getOperator(),0,"审批好友申请");
                return responseVO;
            }
        }

        ApproveFriendRequestPack approverFriendRequestPack = new ApproveFriendRequestPack();
        approverFriendRequestPack.setId(req.getId());
        approverFriendRequestPack.setSequence(seq);
        approverFriendRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(),req.getClientType(),req.getImei(), FriendshipEventCommand.FRIEND_REQUEST_APPROVER,approverFriendRequestPack,req.getAppId());
        storeLog(req.getAppId(),req.getOperator(),1,"审批好友申请");
        return ResponseVO.successResponse("申请已审批");
    }

    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());

        long seq = redisSeq.doGetSeq(req.getAppId()+":"+ Constants.SeqConstants.FriendshipRequest);
        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        update.setSequence(seq);
        imFriendShipRequestMapper.update(update, query);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(), Constants.SeqConstants.FriendshipRequest,seq);
//        TCP通知
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        readAllFriendRequestPack.setSequence(seq);
        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImei(),FriendshipEventCommand.FRIEND_REQUEST_READ,readAllFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }

    private void storeLog(Integer appId, String userId ,int status,String text) {
        SysLog sysLog = new SysLog();
        Object obj = redisTemplate.opsForHash().get(appId + Constants.RedisConstants.UserSessionConstants + userId, "1:windows");
        JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
        sysLog.setUserId(object.getString("userId"));
        sysLog.setAppId(Integer.valueOf(object.getString("appId")));
        sysLog.setBrokerHost(object.getString("brokerHost"));
        sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
        sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));
        sysLog.setClientType(Integer.valueOf(object.getString("clientType")));
        sysLog.setStatus(status);
        sysLog.setImei(object.getString("imei"));
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
    }

}
