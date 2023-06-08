package com.lld.im.service.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.system.dao.ImSysNotificationEntity;
import com.lld.im.service.system.dao.mapper.ImSystemNotificationMapper;
import com.lld.im.service.system.model.req.GetNotificationReq;
import com.lld.im.service.system.model.req.HandleNotifyReq;
import com.lld.im.service.system.model.req.SendNotifyReq;
import com.lld.im.service.system.model.req.UpdateNotifyReq;
import com.lld.im.service.system.service.ImSystemNotificationService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.utils.MessageProducer;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@Service
public class ImSystemNotificationServiceImpl extends ServiceImpl<ImSystemNotificationMapper, ImSysNotificationEntity> implements ImSystemNotificationService {

    @Autowired
    ImSystemNotificationMapper imSystemNotificationMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImUserDataMapper imUserDataMapper;


    @Override
    public ResponseVO getNotification(GetNotificationReq req) {
        if(ObjectUtils.isEmpty(req.getPage()) && ObjectUtils.isEmpty(req.getPageSize())){
            LambdaQueryWrapper<ImSysNotificationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImSysNotificationEntity::getType,1);
            wrapper.eq(ImSysNotificationEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            wrapper.or(query -> query.eq(ImSysNotificationEntity::getFromId,req.getFromId()).eq(ImSysNotificationEntity::getType,2));
            wrapper.or(query -> query.eq(ImSysNotificationEntity::getToId,req.getFromId()).eq(ImSysNotificationEntity::getType,2));

            List<ImSysNotificationEntity> imSysNotificationEntities = imSystemNotificationMapper.selectList(wrapper);

            return ResponseVO.successResponse(imSysNotificationEntities);
        }else {
            IPage<ImSysNotificationEntity> page = new Page<>(req.getPage(),req.getPageSize());

            LambdaQueryWrapper<ImSysNotificationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(req.getType() != 0,ImSysNotificationEntity::getType,req.getType());
            wrapper.eq(StringUtils.isNotBlank(req.getFromId()),ImSysNotificationEntity::getFromId,req.getFromId());
            wrapper.eq(StringUtils.isNotBlank(req.getToId()),ImSysNotificationEntity::getToId,req.getToId());
            wrapper.eq(!ObjectUtils.isEmpty(req.getStatus()),ImSysNotificationEntity::getStatus,req.getStatus());
            wrapper.like(StringUtils.isNotBlank(req.getOperator()),ImSysNotificationEntity::getOperatorId,req.getOperator());
            wrapper.eq(ImSysNotificationEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            if(!CollectionUtils.isEmpty(req.getCreateTime())){
                wrapper.between(ImSysNotificationEntity::getCreateTime,new Timestamp(req.getCreateTime().get(0).getTime()),new Timestamp(req.getCreateTime().get(1).getTime()));
            }

            return ResponseVO.successResponse(imSystemNotificationMapper.selectPage(page,wrapper));
        }
    }


    @Transactional
    @Override
    public ResponseVO createNotify(HttpServletRequest request,SendNotifyReq req) {
        ImSysNotificationEntity imSysNotify = new ImSysNotificationEntity();
        BeanUtils.copyProperties(req,imSysNotify);
        if(req.getIsAdmin() == 1){
            imSysNotify.setOperatorId(req.getOperator());
            imSysNotify.setStatus(1);
            imSystemNotificationMapper.insert(imSysNotify);
            storeLog(request,req.getAppId(), req.getOperator(), 1,"创建通知");
            return ResponseVO.successResponse();
        }
        LambdaQueryWrapper<ImSysNotificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSysNotificationEntity::getFromId,req.getFromId());
        wrapper.eq(ImSysNotificationEntity::getToId,req.getToId());
        wrapper.eq(ImSysNotificationEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
        ImSysNotificationEntity imSysNotificationEntity = imSystemNotificationMapper.selectOne(wrapper);
        if(req.getType() == 2 && req.getTitle() == "用户举报"){
            messageProducer.sendToUser(req.getToId(),UserEventCommand.USER_NOTIFY,imSysNotify,req.getAppId());
        }
        if(!ObjectUtils.isEmpty(imSysNotificationEntity)){
            imSystemNotificationMapper.update(imSysNotify,wrapper);
            storeLog(req.getAppId(),req.getFromId(),1,"发送通知");
            return ResponseVO.successResponse();
        }
        imSystemNotificationMapper.insert(imSysNotify);
        storeLog(req.getAppId(),req.getFromId(),1,"发送通知");
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO deleteNotify(HttpServletRequest request, Integer id, Integer appId,String operator) {
        ImSysNotificationEntity imSysNotificationEntity = imSystemNotificationMapper.selectById(id);
        imSysNotificationEntity.setDelFlag(DelFlagEnum.DELETE.getCode());
        imSystemNotificationMapper.updateById(imSysNotificationEntity);
        storeLog(request,appId, operator, 1,"删除通知");
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO sendNotify(HttpServletRequest request,Integer appId, Integer id,String operator) {
        ImSysNotificationEntity imSysNotify = imSystemNotificationMapper.selectById(id);
        if (imSysNotify.getStatus() == 0){
            storeLog(request,appId, operator, 0,"发送通知");
            return ResponseVO.errorResponse(500,"通知还未处理，无法发送");
        }
        if(imSysNotify.getType() != 2){
            LambdaQueryWrapper<ImUserDataEntity>  wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            wrapper.ne(ImUserDataEntity::getUserId,imSysNotify.getOperatorId());
            List<ImUserDataEntity> imUserDataEntities = imUserDataMapper.selectList(wrapper);
            imUserDataEntities.forEach(e->{
                messageProducer.sendToUser(e.getUserId(),UserEventCommand.ADMIN_NOTIFY,imSysNotify,e.getAppId());
            });
        }else {
            if(imSysNotify.getFromId() != null){
                messageProducer.sendToUser(imSysNotify.getFromId(),UserEventCommand.ADMIN_NOTIFY,imSysNotify,appId);
            }
            if (imSysNotify.getToId() != null){
                messageProducer.sendToUser(imSysNotify.getToId(),UserEventCommand.ADMIN_NOTIFY,imSysNotify,appId);
            }
        }
        storeLog(request,appId, imSysNotify.getOperatorId(), 1,"发送通知");
        return ResponseVO.successResponse();

    }

    @Override
    @Transactional
    public ResponseVO updateNotify(HttpServletRequest request, UpdateNotifyReq req) {
        ImSysNotificationEntity imSysNotification = imSystemNotificationMapper.selectById(req.getId());
        if(imSysNotification == null){
            storeLog(request,req.getAppId(), imSysNotification.getOperatorId(), 0,"修改通知");
            return ResponseVO.errorResponse(500,"通知不存在，修改失败");
        }
        BeanUtils.copyProperties(req,imSysNotification);
        imSystemNotificationMapper.updateById(imSysNotification);
        storeLog(request,req.getAppId(), imSysNotification.getOperatorId(), 1,"修改通知");
        return ResponseVO.successResponse(imSysNotification);
    }

    @Override
    public ResponseVO handleNotify(HttpServletRequest request, HandleNotifyReq req) {
        ImSysNotificationEntity imSysNotification = imSystemNotificationMapper.selectById(req.getId());
        if(imSysNotification == null){
            storeLog(request,req.getAppId(), req.getOperator(), 0,"处理通知");
            return ResponseVO.errorResponse(500,"通知不存在，无法处理");
        }

        if(imSysNotification.getStatus() == 1){
            storeLog(request,req.getAppId(), req.getOperator(), 0,"处理通知");
            return ResponseVO.errorResponse(500,"消息已被处理");
        }
        imSysNotification.setOperatorId(req.getOperator());
        imSysNotification.setFeedBack(req.getFeedBack());
        imSysNotification.setStatus(1);
        imSystemNotificationMapper.updateById(imSysNotification);
        storeLog(request,req.getAppId(), req.getOperator(), 1,"处理通知");
        if(imSysNotification.getFromId() != null){
            messageProducer.sendToUser(imSysNotification.getFromId(),UserEventCommand.ADMIN_NOTIFY,imSysNotification,req.getAppId());
        }
        if (imSysNotification.getToId() != null){
            messageProducer.sendToUser(imSysNotification.getToId(),UserEventCommand.ADMIN_NOTIFY,imSysNotification,req.getAppId());
        }
        return ResponseVO.successResponse(imSysNotification);
    }


    private void storeLog(HttpServletRequest request,Integer appId, String userId ,int status,String text) {
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
