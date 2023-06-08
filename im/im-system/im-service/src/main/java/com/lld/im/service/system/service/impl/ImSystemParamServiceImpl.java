package com.lld.im.service.system.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.system.dao.ImSysNotificationEntity;
import com.lld.im.service.system.dao.ImSysParamEntity;
import com.lld.im.service.system.dao.mapper.ImSystemParamMapper;
import com.lld.im.service.system.model.req.GetParamsReq;
import com.lld.im.service.system.model.req.ModifyParamsReq;
import com.lld.im.service.system.model.req.ParamsReq;
import com.lld.im.service.system.service.ImSystemParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Service
public class ImSystemParamServiceImpl extends ServiceImpl<ImSystemParamMapper, ImSysParamEntity> implements ImSystemParamService {


    @Autowired
    private ImSystemParamMapper imSystemParamMapper;


    @Autowired
    RedisTemplate redisTemplate;



    @Override
    public ResponseVO getParams(GetParamsReq req) {
        IPage<ImSysParamEntity> page = new Page<>(req.getPage(), req.getPageSize());
        LambdaQueryWrapper<ImSysParamEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(req.getName()),ImSysParamEntity::getName, req.getName());
        wrapper.eq(!ObjectUtils.isEmpty(req.getIsSys()),ImSysParamEntity::getIsSys, req.getIsSys());
        wrapper.eq(ImSysParamEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
        if(!CollectionUtils.isEmpty(req.getCreateTime())){
            wrapper.between(ImSysParamEntity::getCreateTime,new Timestamp(req.getCreateTime().get(0).getTime()),new Timestamp(req.getCreateTime().get(1).getTime()));
        }

        return ResponseVO.successResponse(imSystemParamMapper.selectPage(page,wrapper));
    }

    @Override
    public ResponseVO createParam(HttpServletRequest request, ParamsReq req) {
        ImSysParamEntity param = new ImSysParamEntity();
        BeanUtils.copyProperties(req,param);
        int insert = imSystemParamMapper.insert(param);
        if(insert > 0){
            storeLog(request,req.getAppId(),req.getOperator(),1,"新增系统参数");
            return ResponseVO.successResponse();
        }
        storeLog(request,req.getAppId(),req.getOperator(),0,"新增系统参数");
        return ResponseVO.errorResponse(500,"新增系统参数失败");
    }

    @Override
    public ResponseVO updateParam(HttpServletRequest request, ModifyParamsReq req) {
        ImSysParamEntity paramEntity = imSystemParamMapper.selectById(req.getId());
        if (ObjectUtils.isEmpty(paramEntity)){
            storeLog(request,req.getAppId(),req.getOperator(),0,"修改系统参数");
            return ResponseVO.errorResponse(500,"修改系统参数失败");
        }
        BeanUtils.copyProperties(req,paramEntity);
        imSystemParamMapper.updateById(paramEntity);
        storeLog(request,req.getAppId(),req.getOperator(),1,"修改系统参数");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteParam(HttpServletRequest request, Integer id, Integer appId, String operator) {
        ImSysParamEntity paramEntity = imSystemParamMapper.selectById(id);
        if (ObjectUtils.isEmpty(paramEntity)){
            storeLog(request,appId,operator,0,"删除系统参数");
            return ResponseVO.errorResponse(500,"删除系统参数失败");
        }
        paramEntity.setDelFlag(DelFlagEnum.DELETE.getCode());
        imSystemParamMapper.updateById(paramEntity);
        storeLog(request,appId,operator,1,"删除系统参数");
        return ResponseVO.successResponse();
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
}
