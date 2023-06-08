package com.lld.im.service.role.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.RoleTypeEnum;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.role.dao.ImRoleEntity;
import com.lld.im.service.role.dao.ImRoleMenuEntity;
import com.lld.im.service.role.dao.mapper.ImRoleMapper;
import com.lld.im.service.role.dao.mapper.ImRoleMenuMapper;
import com.lld.im.service.role.model.req.*;
import com.lld.im.service.role.service.ImRoleService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class ImRoleServiceImpl extends ServiceImpl<ImRoleMapper, ImRoleEntity> implements ImRoleService {


    @Autowired
    private ImUserDataMapper imUserDataMapper;

    @Autowired
    private ImRoleMapper imRoleMapper;

    @Autowired
    private ImRoleMenuMapper imRoleMenuMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public ResponseVO getByPage(GetRoleReq req){
       if(req.getPage() !=0 && req.getPageSize() != 0){
           IPage<ImRoleEntity> page = new Page<>(req.getPage(), req.getPageSize());
           LambdaQueryWrapper<ImRoleEntity> wrapper = new LambdaQueryWrapper<>();
           wrapper.like(StringUtils.isNotEmpty(req.getName()),ImRoleEntity::getName,req.getName());
           wrapper.eq(req.getStatus() != 2,ImRoleEntity::getStatus,req.getStatus());
           wrapper.eq(ImRoleEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
           wrapper.orderByAsc(ImRoleEntity::getSort);

           IPage<ImRoleEntity> imRoleEntityIPage = imRoleMapper.selectPage(page, wrapper);

           List<ImRoleEntity> records = imRoleEntityIPage.getRecords();
           if(records.size() > 0 ){
               records.forEach(e->{
                   LambdaQueryWrapper<ImUserDataEntity>  queryWrapper = new LambdaQueryWrapper<>();
                   queryWrapper.eq(ImUserDataEntity::getRoleId,e.getRoleId());
                   queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
                   e.getParam().put("userCount",imUserDataMapper.selectCount(queryWrapper));
               });
           }
           return ResponseVO.successResponse(imRoleEntityIPage);
       }else{
           LambdaQueryWrapper<ImRoleEntity> wrapper = new LambdaQueryWrapper<>();
           wrapper.eq(ImRoleEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
           wrapper.orderByAsc(ImRoleEntity::getSort);
           List<ImRoleEntity> imRoleEntities = imRoleMapper.selectList(wrapper);
           if(imRoleEntities.size() > 0 ){
               imRoleEntities.forEach(e->{
                   LambdaQueryWrapper<ImUserDataEntity>  queryWrapper = new LambdaQueryWrapper<>();
                   queryWrapper.eq(ImUserDataEntity::getRoleId,e.getRoleId());
                   queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

                   e.getParam().put("userCount",imUserDataMapper.selectCount(queryWrapper));
               });
           }
           return ResponseVO.successResponse(imRoleEntities);
       }
    }

    @Override
    public ResponseVO updateRoleById(HttpServletRequest request,UpdateRoleReq req) {
        ImRoleEntity imRole = imRoleMapper.selectById(req.getRoleId());
        imRole.setName(req.getName());
        imRole.setRemark(req.getRemark());
        imRole.setSort(req.getSort());
        imRole.setExtent(req.getExtent());
        imRole.setStatus(req.getStatus());
        imRoleMapper.updateById(imRole);
        storeLog(request,10000,req.getOperator(),1,"修改角色");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO updateRoleStatus(HttpServletRequest request,int roleId, int status,String operator) {
        ImRoleEntity imRoleEntity = imRoleMapper.selectById(roleId);
        imRoleEntity.setStatus(status);
        imRoleMapper.updateById(imRoleEntity);
        if (status == 0){
            storeLog(request,10000,operator,1,"禁用角色");
        }else {
            storeLog(request,10000,operator,1,"启用角色");
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO bindRole(HttpServletRequest request,BindRoleReq req) {

        LambdaQueryWrapper<ImUserDataEntity>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImUserDataEntity::getRoleId,req.getRoleId());
        wrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        List<ImUserDataEntity> imUserDataEntities = imUserDataMapper.selectList(wrapper);
        for (ImUserDataEntity userData : imUserDataEntities) {
            userData.setRoleId(0);
            imUserDataMapper.updateById(userData);
        }

        List<String> users = req.getUsers();
        for (String userId : users) {
            LambdaQueryWrapper<ImUserDataEntity>  queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImUserDataEntity::getUserId,userId);
            queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

            ImUserDataEntity userData = imUserDataMapper.selectOne(queryWrapper);
            userData.setRoleId(req.getRoleId());
            imUserDataMapper.updateById(userData);
        }
        storeLog(request,10000, req.getOperator(), 1,"用户绑定角色");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO createRole(HttpServletRequest request,RoleReq req) {

//        LambdaQueryWrapper<ImUserDataEntity>  wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
//        wrapper.eq(StringUtils.isNotEmpty(req.getOperator()),ImUserDataEntity::getUserId,req.getOperator());
//
//        ImUserDataEntity userData = imUserDataMapper.selectOne(wrapper);
//
//        ImRoleEntity imRole = imRoleMapper.selectById(userData.getRoleId());
//        if(ObjectUtils.isEmpty(imRole)){
//            return ResponseVO.errorResponse(500,"非角色用户");
//        }

        ImRoleEntity imRoleEntity = new ImRoleEntity();
        BeanUtils.copyProperties(req,imRoleEntity);
        int insert = imRoleMapper.insert(imRoleEntity);
        if(insert > 0 ){
            storeLog(request,10000,req.getOperator(),1,"创建角色");
            return ResponseVO.successResponse();
        }
        storeLog(request,10000,req.getOperator(),0,"创建角色");
        return ResponseVO.errorResponse(500,"角色创建失败");
    }

    @Override
    @Transactional
    public ResponseVO deleteRoleById(HttpServletRequest request,DeleteRoleReq req) {
        ImRoleEntity imRoleEntity = imRoleMapper.selectById(req.getRoleId());
        if(ObjectUtils.isNotEmpty(imRoleEntity)){
            if((req.getIsAdmin() == 1 || req.getIsAdmin() == 0) && (RoleTypeEnum.ADMIN.equals(imRoleEntity.getName()) || RoleTypeEnum.SUPER_ADMIN.equals(imRoleEntity.getName()))){
                storeLog(request,10000,req.getOperator(),0,"删除角色");
                return ResponseVO.errorResponse(500,"该操作只限超级管理员");
            }

            LambdaQueryWrapper<ImUserDataEntity>  wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
            wrapper.eq(ImUserDataEntity::getRoleId,imRoleEntity.getRoleId());

            List<ImUserDataEntity> imUserDataEntities = imUserDataMapper.selectList(wrapper);
            imUserDataEntities.forEach(e->{
                e.setRoleId(0);
                imUserDataMapper.updateById(e);
            });

            LambdaQueryWrapper<ImRoleMenuEntity>  query = new LambdaQueryWrapper<>();
            query.eq(ImRoleMenuEntity::getRoleId,imRoleEntity.getRoleId());

            imRoleMenuMapper.delete(query);

            imRoleEntity.setDelFlag(DelFlagEnum.DELETE.getCode());
            imRoleMapper.updateById(imRoleEntity);
            storeLog(request,10000,req.getOperator(),1,"删除角色");
            return ResponseVO.successResponse();

        }
        return ResponseVO.errorResponse(500,"删除角色失败");
    }

    private void storeLog(HttpServletRequest request, Integer appId, String userId , int status, String text) {
        SysLog sysLog = new SysLog();
        sysLog.setUserId(userId);
        sysLog.setAppId(appId);
        sysLog.setImei("windows");
        sysLog.setBrokerHost(request.getRemoteHost());
        sysLog.setConnectState(1);
        sysLog.setClientType(1);
        sysLog.setStatus(status);
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants, JSONObject.toJSONString(sysLog),new Date().getTime());
    }
}
