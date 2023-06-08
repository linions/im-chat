package com.lld.im.service.group.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.lld.im.service.group.controller.ImGroupRequestController;
import com.lld.im.service.group.dao.ImGroupRequestEntity;
import com.lld.im.service.group.model.req.ApproveGroupRequestReq;
import com.lld.im.service.group.model.req.GetGroupRequestReq;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.model.req.ReadGroupRequestReq;


public interface ImGroupRequestService extends IService<ImGroupRequestEntity> {

    ResponseVO approveGroupRequest(ApproveGroupRequestReq req);

    ResponseVO getGroupRequest(GetGroupRequestReq req);

//    ResponseVO readGroupRequestReq(ReadGroupRequestReq req);

    ResponseVO addGroupRequest(String groupId, GroupMemberDto dto, Integer appId);
}
