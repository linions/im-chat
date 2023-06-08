package com.lld.im.service.system.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
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
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageFileEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageFileMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.role.dao.ImMenuEntity;
import com.lld.im.service.system.dao.ImSysNotificationEntity;
import com.lld.im.service.system.model.req.*;
import com.lld.im.service.system.model.resp.SysDataResp;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.ObjectUtils;
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
public interface ImSystemNotificationService extends IService<ImSysNotificationEntity> {

    ResponseVO getNotification(GetNotificationReq req);

    ResponseVO createNotify(HttpServletRequest request,SendNotifyReq req);

    ResponseVO deleteNotify(HttpServletRequest request, Integer id, Integer appId,String operator);

    ResponseVO sendNotify(HttpServletRequest request,Integer appId, Integer id,String operator);

    ResponseVO updateNotify(HttpServletRequest request, UpdateNotifyReq req);

    ResponseVO handleNotify(HttpServletRequest request, HandleNotifyReq req);
}
