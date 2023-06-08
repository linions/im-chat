package com.lld.im.service.role.service.impl;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.service.role.dao.ImRoleEntity;
import com.lld.im.service.role.dao.ImRoleMenuEntity;
import com.lld.im.service.role.dao.mapper.ImRoleMapper;
import com.lld.im.service.role.dao.mapper.ImRoleMenuMapper;
import com.lld.im.service.role.service.ImRoleMenuService;
import com.lld.im.service.role.service.ImRoleService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Service
public class ImRoleMenuServiceImpl extends ServiceImpl<ImRoleMenuMapper, ImRoleMenuEntity> implements ImRoleMenuService {
}
