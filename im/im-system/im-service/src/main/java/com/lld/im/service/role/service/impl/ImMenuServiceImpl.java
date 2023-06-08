package com.lld.im.service.role.service.impl;


import cn.hutool.db.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.RoleTypeEnum;
import com.lld.im.service.role.dao.ImMenuEntity;
import com.lld.im.service.role.dao.ImRoleEntity;
import com.lld.im.service.role.dao.ImRoleMenuEntity;
import com.lld.im.service.role.dao.mapper.ImMenuMapper;
import com.lld.im.service.role.dao.mapper.ImRoleMapper;
import com.lld.im.service.role.dao.mapper.ImRoleMenuMapper;
import com.lld.im.service.role.model.req.DeleteRoleReq;
import com.lld.im.service.role.model.req.GetRoleReq;
import com.lld.im.service.role.model.req.RoleReq;
import com.lld.im.service.role.service.ImMenuService;
import com.lld.im.service.role.service.ImRoleService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImMenuServiceImpl extends ServiceImpl<ImMenuMapper, ImMenuEntity> implements ImMenuService {

}
