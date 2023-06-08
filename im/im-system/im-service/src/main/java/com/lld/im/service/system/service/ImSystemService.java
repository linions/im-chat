package com.lld.im.service.system.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.service.friendship.model.req.GetAllFriendShipReq;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.lld.im.service.group.dao.mapper.ImGroupRequestMapper;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageFileEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageFileMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.system.model.req.DestroyUserReq;
import com.lld.im.service.system.model.req.LogReq;
import com.lld.im.service.system.model.req.SysDataReq;
import com.lld.im.service.system.model.resp.SysDataResp;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.user.service.ImUserStatusService;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ImSystemService {

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendService imFriendService;


    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImUserDataMapper imUserDataMapper;

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;


    @Autowired
    ImGroupRequestMapper imGroupRequestMapper;

    @Autowired
    ImGroupMemberMapper imGroupMemberMapper;

    @Autowired
    ImMessageFileMapper imMessageFileMapper;

    @Autowired
    RedisTemplate redisTemplate;


    public ResponseVO getSysData(SysDataReq req) {
        SysDataResp sysDataResp = new SysDataResp();

        if(req.getIsAdmin() != 0){
            LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
            queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

            Integer userCount = imUserService.getBaseMapper().selectCount(queryWrapper);
            sysDataResp.setUserCount(Long.valueOf(userCount));

            LambdaQueryWrapper<ImMessageBodyEntity> query = new LambdaQueryWrapper<>();
            query.eq(ImMessageBodyEntity::getAppId,req.getAppId());
            query.eq(ImMessageBodyEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
            Integer msgCount = imMessageBodyMapper.selectCount(query);

            sysDataResp.setMsgCount(Long.valueOf(msgCount));


            LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
            wrapper.eq(ImUserDataEntity::getStatus,1);
            wrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

            Integer onlineCount = imUserService.getBaseMapper().selectCount(wrapper);

            sysDataResp.setOnlineUser(Long.valueOf(onlineCount));
        }else {
            GetAllFriendShipReq getAllFriendShipReq = new GetAllFriendShipReq();
            getAllFriendShipReq.setAppId(req.getAppId());
            getAllFriendShipReq.setFromId(req.getUserId());
            ResponseVO allFriendShip = imFriendService.getAllFriendShip(getAllFriendShipReq);
            if(allFriendShip.getData() != null){

                List<ImFriendShipEntity> friendShips = (List<ImFriendShipEntity>) allFriendShip.getData();
                sysDataResp.setFriendCount(Long.valueOf(friendShips.size()));

                AtomicInteger onlineCount = new AtomicInteger();
                friendShips.forEach(e->{
                    LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
                    wrapper.eq(ImUserDataEntity::getUserId,e.getToId());
                    wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
                    ImUserDataEntity userData = imUserService.getBaseMapper().selectOne(wrapper);
                    if(ObjectUtils.isNotEmpty(userData) && userData.getStatus() == 1){
                        onlineCount.getAndIncrement();
                    }

                });
                sysDataResp.setOnlineFriend(onlineCount.longValue());

            }else {
                sysDataResp.setFriendCount(0L);
                sysDataResp.setOnlineFriend(0L);

            }

            LambdaQueryWrapper<ImMessageHistoryEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImMessageHistoryEntity::getAppId,req.getAppId());
            wrapper.eq(ImMessageHistoryEntity::getOwnerId,req.getUserId());
            wrapper.eq(ImMessageHistoryEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());


            Integer msgCount = imMessageHistoryMapper.selectCount(wrapper);
            sysDataResp.setMsgCount(Long.valueOf(msgCount));

        }

        return ResponseVO.successResponse(sysDataResp);

    }


    @Transactional
    public ResponseVO destroyUserByAdmin(HttpServletRequest request, String userId,String operator) {
        LambdaQueryWrapper<ImUserDataEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImUserDataEntity::getUserId,userId);
        ImUserDataEntity userData = imUserDataMapper.selectOne(query);

        userData.setDelFlag(DelFlagEnum.DELETE.getCode());
        imUserDataMapper.updateById(userData);

        imFriendShipMapper.destroyUser(userId);
        imConversationSetMapper.destroyUser(userId);
        imGroupMemberMapper.destroyUser(userId);
        imFriendShipRequestMapper.destroyUser(userId);
        imGroupRequestMapper.destroyUser(userId);
        List<String> strings = imMessageHistoryMapper.destroyUser(userId);
        strings.forEach(e->{
            LambdaQueryWrapper<ImMessageBodyEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImMessageBodyEntity::getMessageKey,e);
            wrapper.eq(ImMessageBodyEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(wrapper);
            if(ObjectUtils.isNotEmpty(messageBody)){
                messageBody.setDelFlag(DelFlagEnum.DELETE.getCode());
                imMessageBodyMapper.updateById(messageBody);
                if(messageBody.getType() == 2 || messageBody.getType() ==3 ){
                    LambdaQueryWrapper<ImMessageFileEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ImMessageFileEntity::getFileId,messageBody.getFileId());
                    imMessageFileMapper.delete(queryWrapper);
                }
            }
        });
        storeLog(request,10000,operator,1,"删除用户");
        return ResponseVO.successResponse();
    }

    public ResponseVO destroyUser(DestroyUserReq req) {
        if (req.getType() == 2){
            String mobile = req.getMobile();
            String code = req.getCode();
            String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + mobile;
            String redisCode = (String) redisTemplate.opsForValue().get(key);
            if (redisCode != null && redisCode.equals(code)) {
                return destroyUserByAdmin(null,req.getUserId(),req.getUserId());
            }
        }

        if (req.getType() == 3){
            String email = req.getEmail();
            String code = req.getCode();
            String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + email;
            String redisCode = (String) redisTemplate.opsForValue().get(key);
            if (redisCode != null && redisCode.equals(code)) {
                return destroyUserByAdmin(null,req.getUserId(),req.getUserId());
            }
        }
        storeLog(req.getAppId(), req.getUserId(),0,"注销账号");
        return ResponseVO.errorResponse(500,"注销失败，请联系管理员处理");
    }

    private void storeLog(HttpServletRequest request,Integer appId, String userId ,int status,String text) {
        if(request == null){
            storeLog(10000,userId,1,"注销账号");
        }else{
            SysLog sysLog = new SysLog();
            sysLog.setUserId(userId);
            sysLog.setAppId(appId);
            sysLog.setBrokerHost(request.getRemoteHost());
            sysLog.setConnectState(1);
            sysLog.setClientType(1);
            sysLog.setImei("windows");
            sysLog.setStatus(status);
            sysLog.setTime(new Timestamp(new Date().getTime()));
            sysLog.setOperate(text);
            //存储log
            redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants, JSONObject.toJSONString(sysLog),new Date().getTime());
        }
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

    public ResponseVO getLog(LogReq req) {
        List<SysLog> sysLogs = new ArrayList<>();
        String key =  req.getAppId() + ":" + Constants.RedisConstants.SysLogConstants;
        Set range = redisTemplate.opsForZSet().range(key, 0, -1);
        range.forEach(e->{
            SysLog sysLog = JSONObject.parseObject(e.toString(), SysLog.class);
            System.out.println(sysLog.getUserId());
            if (req.getUserId() != null && StringUtils.isNotEmpty(sysLog.getUserId()) && sysLog.getUserId().equals(req.getUserId())){
                sysLogs.add(sysLog);
            }
            if (!CollectionUtils.isEmpty(req.getTime()) && (sysLog.getTime().after(req.getTime().get(0)) && sysLog.getTime().before(req.getTime().get(1)))){
                sysLogs.add(sysLog);
            }
            if(CollectionUtils.isEmpty(req.getTime()) && req.getUserId() == null){
                sysLogs.add(sysLog);
            }
        });
        return ResponseVO.successResponse(sysLogs);
    }

}
