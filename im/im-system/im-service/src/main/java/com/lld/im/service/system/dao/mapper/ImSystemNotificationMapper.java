package com.lld.im.service.system.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.system.dao.ImSysNotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;


@Mapper
public interface ImSystemNotificationMapper extends BaseMapper<ImSysNotificationEntity> {

}
