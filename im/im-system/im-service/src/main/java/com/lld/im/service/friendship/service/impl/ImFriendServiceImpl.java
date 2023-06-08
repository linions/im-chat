package com.lld.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.friendship.*;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.*;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.friendship.model.callback.AddFriendAfterCallbackDto;
import com.lld.im.service.friendship.model.callback.AddFriendBlackAfterCallbackDto;
import com.lld.im.service.friendship.model.callback.DeleteFriendAfterCallbackDto;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.model.resp.CheckFriendShipResp;
import com.lld.im.service.friendship.model.resp.ImportFriendShipResp;
import com.lld.im.service.friendship.model.resp.SearchFriendResp;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.user.model.req.GetUserInfoReq;
import com.lld.im.service.user.model.resp.GetSingleUserInfoResp;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ImFriendServiceImpl extends ServiceImpl<ImFriendShipMapper, ImFriendShipEntity> implements ImFriendService {

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public ResponseVO importFriendShip(ImportFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImportFriendShipReq.ImportFriendDto dto :
                req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {

        ResponseVO<GetSingleUserInfoResp> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
//        找不到加好友from用户
        if (!fromInfo.isOk()) {
            return fromInfo;
        }
//        找不到加好友to用户
        ResponseVO<GetSingleUserInfoResp> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }
//        之前回调
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO callbackResp = callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.AddFriendBefore, JSONObject.toJSONString(req));
            if (!callbackResp.isOk()) {
                return callbackResp;
            }
        }

        GetSingleUserInfoResp data = toInfo.getData();

        if (data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            storeLog(req.getAppId() ,req.getFromId(),1,"添加好友");
            return this.doAddFriend(req, req.getFromId(), req.getToItem(), req.getAppId());
        } else {
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("to_id", req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if (fromItem == null || fromItem.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                //插入一条好友申请的数据
                ResponseVO responseVO = imFriendShipRequestService.addFriendshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    storeLog(req.getAppId() ,req.getFromId(),0,"添加好友");
                    return ResponseVO.errorResponse(500,"对方无法直接添加，需通过好友申请");
                }
            } else {
                storeLog(req.getAppId() ,req.getFromId(),0,"添加好友");
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }

        }

        return ResponseVO.successResponse("好友申请已发送");
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {

        ResponseVO<GetSingleUserInfoResp> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<GetSingleUserInfoResp> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
        if (responseVO.isOk()) {
            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setRemark(req.getToItem().getRemark());
            updateFriendPack.setToId(req.getToItem().getToId());

            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_UPDATE, updateFriendPack, req.getAppId());

            if (appConfig.isModifyFriendAfterCallback()) {
                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());
                callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.UpdateFriendAfter, JSONObject.toJSONString(callbackDto));
            }
        }
        storeLog(req.getAppId() ,req.getFromId(),responseVO.isOk() == true ? 1 : 0,"修改好友信息");
        return responseVO;
    }

    @Transactional
    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId) {


        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .set(ImFriendShipEntity::getFriendSequence, seq)
                .set(ImFriendShipEntity::getRemark, dto.getRemark())
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getToId, dto.getToId())
                .eq(ImFriendShipEntity::getFromId, fromId);

        int update = imFriendShipMapper.update(null, updateWrapper);
        if (update == 1) {
            writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
            return ResponseVO.successResponse();
        }

        return ResponseVO.errorResponse();
    }

    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId) {

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        long seq = 0L;
        if (fromItem == null) {
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
            fromItem.setAppId(appId);
            fromItem.setFriendSequence(seq);
            fromItem.setFromId(fromId);
//            entity.setToId(to);
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
        } else {
            //如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();

                if (StringUtils.isNotBlank(dto.getAddSource())) {
                    update.setAddSource(dto.getAddSource());
                }

                if (StringUtils.isNotBlank(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }

                if (StringUtils.isNotBlank(dto.getExtra())) {
                    update.setExtra(dto.getExtra());
                }
                seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
                writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
            }

        }

        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id", appId);
        toQuery.eq("from_id", dto.getToId());
        toQuery.eq("to_id", fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        if (ObjectUtils.isEmpty(toItem)) {
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            toItem.setToId(fromId);
            toItem.setFriendSequence(seq);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            imFriendShipMapper.insert(toItem);
            writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship, seq);
        } else {
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toItem.getStatus()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(update, toQuery);
                writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship, seq);
            }
        }

//        //发送给from
        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem, addFriendPack);
        addFriendPack.setSequence(seq);
        if (requestBase != null) {
            messageProducer.sendToUser(fromId, requestBase.getClientType(), requestBase.getImei(), FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        } else {
            messageProducer.sendToUser(fromId, FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        }
        //发送给to
        AddFriendPack addFriendToPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addFriendPack);
        messageProducer.sendToUser(toItem.getFromId(),
                FriendshipEventCommand.FRIEND_ADD, addFriendToPack
                , requestBase.getAppId());

        //之后回调
        if (appConfig.isAddFriendAfterCallback()) {
            AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
            callbackDto.setFromId(fromId);
            callbackDto.setToItem(dto);
            callbackService.beforeCallback(appId,
                    Constants.CallbackCommand.AddFriendAfter, JSONObject
                            .toJSONString(callbackDto));
        }

        return ResponseVO.successResponse("好友添加成功");
    }
//

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {

        LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
        query.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getFromId, req.getFromId());
        query.eq(StringUtils.isNotEmpty(req.getToId()), ImFriendShipEntity::getToId, req.getToId());
        query.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getBlack, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);

//        LambdaQueryWrapper<ImFriendShipEntity> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
//        wrapper.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getToId, req.getFromId());
//        wrapper.eq(StringUtils.isNotEmpty(req.getToId()), ImFriendShipEntity::getFromId, req.getToId());
//        wrapper.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
//        wrapper.eq(ImFriendShipEntity::getBlack, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
//        wrapper.eq(ImFriendShipEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
//
//        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(wrapper);

        if (fromItem == null ) {
            storeLog(req.getAppId() ,req.getFromId(),0,"删除好友");
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
                ImFriendShipEntity update = new ImFriendShipEntity();
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendShipMapper.update(update, query);
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
                DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
                deleteFriendPack.setFromId(req.getFromId());
                deleteFriendPack.setSequence(seq);
                deleteFriendPack.setToId(req.getToId());
                messageProducer.sendToUser(req.getToId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_DELETE, deleteFriendPack, req.getAppId());

                //之后回调
                if (appConfig.isDeleteFriendAfterCallback()) {
                    DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());
                    callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.DeleteFriendAfter, JSONObject.toJSONString(callbackDto));
                }


        }
        storeLog(req.getAppId() ,req.getFromId(),1,"删除好友");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
        query.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getFromId, req.getFromId());
        query.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
//        update.setDelFlag(DelFlagEnum.DELETE.getCode());
        imFriendShipMapper.update(update, query);

        DeleteAllFriendPack deleteFriendPack = new DeleteAllFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_ALL_DELETE, deleteFriendPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {

        List<ImFriendShipEntity> result = new ArrayList<>();

        LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
        query.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getFromId, req.getFromId());
        query.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getBlack, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        List<ImFriendShipEntity> imFriendShipEntities = imFriendShipMapper.selectList(query);
        imFriendShipEntities.forEach(e->{
            LambdaQueryWrapper<ImFriendShipEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
            wrapper.eq(StringUtils.isNotEmpty(e.getFromId()), ImFriendShipEntity::getToId, e.getFromId());
            wrapper.eq(StringUtils.isNotEmpty(e.getToId()), ImFriendShipEntity::getFromId, e.getToId());
            wrapper.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            wrapper.eq(ImFriendShipEntity::getBlack, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            wrapper.eq(ImFriendShipEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

            ImFriendShipEntity entity = imFriendShipMapper.selectOne(wrapper);

            if(ObjectUtils.isNotEmpty(entity)){
                ResponseVO singleUserInfo = imUserService.getSingleUserInfo(e.getToId(), e.getAppId());
                if(ObjectUtils.isNotEmpty(singleUserInfo.getData())){
                    e.getParam().put("userInfo",singleUserInfo.getData());
                    result.add(e);
                }
            }
        });
        return ResponseVO.successResponse(result);
    }

    @Override
    public ResponseVO getRelation(GetRelationReq req) {

        LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
        query.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getFromId, req.getFromId());
        query.eq(StringUtils.isNotEmpty(req.getToId()), ImFriendShipEntity::getToId, req.getToId());
        query.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getBlack, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        query.eq(ImFriendShipEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if (ObjectUtils.isEmpty(entity)) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIENDSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO getRelationWithInfo(GetRelationReq req) {
        String fromId = req.getFromId();
        String toId = req.getToId();
        SearchFriendResp friendResp = new SearchFriendResp();
        LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
        wrapper.eq(StringUtils.isNotEmpty(req.getToId()),ImUserDataEntity::getUserId,req.getToId());
        wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity one = imUserService.getOne(wrapper);
        BeanUtils.copyProperties(one,friendResp);

        ResponseVO fromRelation = getRelation(req);
        if (ObjectUtils.isEmpty(fromRelation.getData())){
            friendResp.setIsFriend(0);
            return ResponseVO.successResponse(friendResp);
        }
        req.setToId(fromId);
        req.setFromId(toId);
        ResponseVO toRelation = getRelation(req);
        if (ObjectUtils.isEmpty(toRelation.getData())){
            friendResp.setIsFriend(0);
            return ResponseVO.successResponse(friendResp);
        }
        friendResp.setIsFriend(1);
        friendResp.setFriendShip((ImFriendShipEntity) toRelation.getData());
        return ResponseVO.successResponse(friendResp);
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {

        Map<String, Integer> result = req.getToIds().stream().collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp;

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream().collect(Collectors.toMap(CheckFriendShipResp::getToId, CheckFriendShipResp::getStatus));

        for (String toId : result.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {

        ResponseVO<GetSingleUserInfoResp> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<GetSingleUserInfoResp> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
        query.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getFromId, req.getFromId());
        query.eq(StringUtils.isNotEmpty(req.getToId()), ImFriendShipEntity::getToId, req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);


        Long seq = 0L;
        if (ObjectUtils.isEmpty(fromItem)) {
            //走添加逻辑。
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setFriendSequence(seq);
            fromItem.setAppId(req.getAppId());
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
            }
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);

        } else {
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                update.setDelFlag(DelFlagEnum.NORMAL.getCode());
                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    storeLog(req.getAppId(),req.getFromId(),0,"添加黑名单");
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
            }
        }

        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        addFriendBlackPack.setFromId(req.getFromId());
        addFriendBlackPack.setSequence(seq);
        addFriendBlackPack.setToId(req.getToId());
        //发送tcp通知
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());

        //之后回调
        if (appConfig.isAddFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
            callbackDto.setFromId(req.getFromId());
            callbackDto.setToId(req.getToId());
            callbackService.beforeCallback(req.getAppId(),Constants.CallbackCommand.AddBlackAfter, JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        LambdaQueryWrapper<ImFriendShipEntity> queryFrom = new LambdaQueryWrapper<>();
        queryFrom.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
        queryFrom.eq(StringUtils.isNotEmpty(req.getFromId()), ImFriendShipEntity::getFromId, req.getFromId());
        queryFrom.eq(StringUtils.isNotEmpty(req.getToId()), ImFriendShipEntity::getToId, req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);

        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

        ImFriendShipEntity imFriendShipEntity = new ImFriendShipEntity();
        imFriendShipEntity.setFriendSequence(seq);
        imFriendShipEntity.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        imFriendShipEntity.setDelFlag(DelFlagEnum.NORMAL.getCode());

        int update = imFriendShipMapper.update(imFriendShipEntity, queryFrom);
        if (update == 1) {
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
            DeleteBlackPack deleteBlackPack = new DeleteBlackPack();
            deleteBlackPack.setFromId(req.getFromId());
            deleteBlackPack.setSequence(seq);
            deleteBlackPack.setToId(req.getToId());
            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_BLACK_DELETE, deleteBlackPack, req.getAppId());

            //之后回调
            if (appConfig.isAddFriendShipBlackAfterCallback()) {
                AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());
                callbackService.beforeCallback(req.getAppId(),
                        Constants.CallbackCommand.DeleteBlack, JSONObject
                                .toJSONString(callbackDto));
            }
        }
        storeLog(req.getAppId(),req.getFromId(),1,"添加黑名单");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO checkBlack(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap = req.getToIds().stream().collect(Collectors.toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result;
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream().collect(Collectors.toMap(CheckFriendShipResp::getToId, CheckFriendShipResp::getStatus));
        for (String toId : toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }

    @Override
    public ResponseVO syncFriendshipList(SyncReq req) {

        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp<ImFriendShipEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getOperator());
        queryWrapper.gt("friend_sequence", req.getLastSequence());
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("friend_sequence");
        List<ImFriendShipEntity> list = imFriendShipMapper.selectList(queryWrapper);

        if (!CollectionUtils.isEmpty(list)) {
            ImFriendShipEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imFriendShipMapper.getFriendShipMaxSeq(req.getAppId(), req.getOperator());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getFriendSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public List<String> getAllFriendId(String userId, Integer appId) {
        return imFriendShipMapper.getAllFriendId(userId,appId);
    }


    //搜索查找好友
    @Override
    public ResponseVO searchFriend(SearchFriendReq req) {
        List<SearchFriendResp> resultList = new ArrayList<>();

        GetUserInfoReq getUserInfoReq = new GetUserInfoReq();
        getUserInfoReq.setUserId(req.getSearchInfo());
        getUserInfoReq.setNickName(req.getSearchInfo());
        ResponseVO userInfos = imUserService.getUserInfo(getUserInfoReq);
        List<ImUserDataEntity> userList = (List<ImUserDataEntity>) userInfos.getData();
        if (CollectionUtils.isNotEmpty(userList)){
            userList.forEach(e->{
                SearchFriendResp searchFriendResp = new SearchFriendResp();
                BeanUtils.copyProperties(e,searchFriendResp);

                GetRelationReq getRelationReq = new GetRelationReq();
                getRelationReq.setFromId(req.getUserId());
                getRelationReq.setAppId(e.getAppId());
                getRelationReq.setToId(e.getUserId());
                ResponseVO relation = this.getRelation(getRelationReq);
                if (relation.isOk()){
                    searchFriendResp.setFriendShip((ImFriendShipEntity) relation.getData());
                    searchFriendResp.setIsFriend(1);
                }else {
                    searchFriendResp.setIsFriend(0);
                }
                if(e.getUserId().equals(req.getUserId())){
                    searchFriendResp.setIsFriend(2);
                }
                searchFriendResp.setType(1);
                resultList.add(searchFriendResp);
            });
        }
        storeLog(req.getAppId(),req.getOperator(),1,"查找好友");
        return ResponseVO.successResponse(resultList);
    }

    private void storeLog(Integer appId, String userId ,int status,String text) {
        SysLog sysLog = new SysLog();
        Object obj = redisTemplate.opsForHash().get(appId + Constants.RedisConstants.UserSessionConstants + userId, "1:windows");
        JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
        sysLog.setUserId(object.getString("userId"));
        sysLog.setAppId(Integer.valueOf(object.getString("appId")));
        sysLog.setImei(object.getString("imei"));
        sysLog.setBrokerHost(object.getString("brokerHost"));
        sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
        sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));
        sysLog.setClientType(Integer.valueOf(object.getString("clientType")));
        sysLog.setStatus(status);
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
    }


}
