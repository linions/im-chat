package com.lld.im.service.group.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.group.model.req.ApproveGroupRequestReq;
import com.lld.im.service.group.model.req.GetGroupRequestReq;
import com.lld.im.service.group.model.req.ReadGroupRequestReq;
import com.lld.im.service.group.service.ImGroupRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("v1/groupRequest")
public class ImGroupRequestController {

    @Autowired
    ImGroupRequestService imGroupRequestService;

    @PutMapping ("/approveGroupRequest")
    public ResponseVO approveGroupRequest(@RequestBody @Validated ApproveGroupRequestReq req){
        return imGroupRequestService.approveGroupRequest(req);
    }

    @PostMapping("/getGroupRequest")
    public ResponseVO getGroupRequest(@RequestBody @Validated GetGroupRequestReq req){
        return imGroupRequestService.getGroupRequest(req);
    }

//    @PostMapping("/readGroupRequestReq")
//    public ResponseVO readGroupRequestReq(@RequestBody @Validated ReadGroupRequestReq req){
//        return imGroupRequestService.readGroupRequestReq(req);
//    }
}
