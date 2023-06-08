package com.lld.im.service.friendship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;


public interface ImFriendShipRequestService extends IService<ImFriendShipRequestEntity> {

    public ResponseVO addFriendshipRequest(String fromId, FriendDto dto, Integer appId);

    public ResponseVO approveFriendRequest(ApproveFriendRequestReq req);

    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    public ResponseVO getFriendRequest(GetFriendShipRequestReq req);
}
