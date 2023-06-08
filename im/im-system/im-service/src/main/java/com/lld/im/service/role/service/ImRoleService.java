package com.lld.im.service.role.service;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.role.dao.ImRoleEntity;
import com.lld.im.service.role.model.req.*;
import org.apache.ibatis.annotations.Mapper;

import javax.servlet.http.HttpServletRequest;

public interface ImRoleService  extends IService<ImRoleEntity> {
    ResponseVO createRole(HttpServletRequest request,RoleReq req);

    ResponseVO deleteRoleById(HttpServletRequest request,DeleteRoleReq req);

    ResponseVO getByPage(GetRoleReq req);

    ResponseVO updateRoleById(HttpServletRequest request,UpdateRoleReq req);

    ResponseVO updateRoleStatus(HttpServletRequest request,int roleId, int status ,String operator);

    ResponseVO bindRole(HttpServletRequest request,BindRoleReq req);
}
