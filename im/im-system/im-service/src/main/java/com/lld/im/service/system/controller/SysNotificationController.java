package com.lld.im.service.system.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.lld.im.service.system.model.req.*;
import com.lld.im.service.system.service.ImSystemNotificationService;
import com.lld.im.service.system.service.ImSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping("v1/system/notify")
public class SysNotificationController {


    @Autowired
    ImSystemNotificationService imSystemNotificationService;

    @PostMapping("/getByPage")
    public ResponseVO getByPage(@RequestBody  GetNotificationReq req){
        return imSystemNotificationService.getNotification(req);
    }

    @PostMapping("/create")
    public ResponseVO createNotify(HttpServletRequest request,@RequestBody @Validated SendNotifyReq req){
        return imSystemNotificationService.createNotify(request,req);
    }

    @PostMapping("/send/{appId}/{id}/{operator}")
    public ResponseVO sendNotify(HttpServletRequest request,@PathVariable Integer appId,@PathVariable Integer id ,@PathVariable String operator){
        return imSystemNotificationService.sendNotify(request,appId,id,operator);
    }

    @PutMapping("/update")
    public ResponseVO updateNotify(HttpServletRequest request,@RequestBody @Validated UpdateNotifyReq req){
        return imSystemNotificationService.updateNotify(request,req);
    }

    @PutMapping("/handle")
    public ResponseVO handleNotify(HttpServletRequest request,@RequestBody HandleNotifyReq req){
        return imSystemNotificationService.handleNotify(request,req);
    }


    @DeleteMapping("/delete/{id}/{appId}/{operator}")
    public ResponseVO deleteNotify(HttpServletRequest request,@PathVariable Integer id ,@PathVariable Integer appId ,@PathVariable String operator){
        return imSystemNotificationService.deleteNotify(request,id,appId,operator);
    }
}
