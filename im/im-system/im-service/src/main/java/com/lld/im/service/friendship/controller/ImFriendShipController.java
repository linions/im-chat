package com.lld.im.service.friendship.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.service.ImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("v1/friendship")
public class ImFriendShipController {

    @Autowired
    ImFriendService imFriendShipService;

    @PostMapping("/importFriendShip")
    public ResponseVO importFriendShip(@RequestBody @Validated ImportFriendShipReq req){
        return imFriendShipService.importFriendShip(req);
    }

    @PostMapping("/searchFriend")
    public ResponseVO searchFriend(@RequestBody @Validated SearchFriendReq req){
        return imFriendShipService.searchFriend(req);
    }

    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody @Validated AddFriendReq req){
        return imFriendShipService.addFriend(req);
    }

    @PostMapping("/updateFriend")
    public ResponseVO updateFriend(@RequestBody @Validated UpdateFriendReq req){
        return imFriendShipService.updateFriend(req);
    }

    @PostMapping("/deleteFriend")
    public ResponseVO deleteFriend(@RequestBody @Validated DeleteFriendReq req){
        return imFriendShipService.deleteFriend(req);
    }

    @RequestMapping("/deleteAllFriend")
    public ResponseVO deleteAllFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipService.deleteAllFriend(req);
    }

    @PostMapping("/getAllFriendShip")
    public ResponseVO getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req){
        return imFriendShipService.getAllFriendShip(req);
    }

    @PostMapping("/getRelation")
    public ResponseVO getRelation(@RequestBody @Validated GetRelationReq req){
        return imFriendShipService.getRelation(req);
    }

    @PostMapping("/getRelationWithInfo")
    public ResponseVO getRelationWithInfo(@RequestBody @Validated GetRelationReq req){
        return imFriendShipService.getRelationWithInfo(req);
    }

    @PostMapping("/checkFriend")
    public ResponseVO checkFriend(@RequestBody @Validated CheckFriendShipReq req){
        return imFriendShipService.checkFriendship(req);
    }

    @PostMapping("/addBlack")
    public ResponseVO addBlack(@RequestBody @Validated AddFriendShipBlackReq req){
        return imFriendShipService.addBlack(req);
    }

    @PostMapping("/deleteBlack")
    public ResponseVO deleteBlack(@RequestBody @Validated DeleteBlackReq req){
        return imFriendShipService.deleteBlack(req);
    }

    @PostMapping("/checkBlack")
    public ResponseVO checkBlack(@RequestBody @Validated CheckFriendShipReq req){
        return imFriendShipService.checkBlack(req);
    }


    @PostMapping("/syncFriendshipList")
    public ResponseVO syncFriendshipList(@RequestBody @Validated SyncReq req){
        return imFriendShipService.syncFriendshipList(req);
    }
}
