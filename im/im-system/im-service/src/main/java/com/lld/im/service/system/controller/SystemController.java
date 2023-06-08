package com.lld.im.service.system.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.system.model.req.DestroyUserReq;
import com.lld.im.service.system.model.req.LogReq;
import com.lld.im.service.system.model.req.SysDataReq;
import com.lld.im.service.system.service.ImSystemService;
import com.lld.im.service.user.model.req.GetUserInfoReq;
import com.lld.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping("v1/system")
public class SystemController {


    @Autowired
    ImSystemService imSystemService;

    @PostMapping("/getSysData")
    public ResponseVO getSysData(@RequestBody SysDataReq req){
        return imSystemService.getSysData(req);
    }

    //管理员
    @PostMapping("/destroyUser/{userId}/{operator}")
    public ResponseVO destroyUserByAdmin(HttpServletRequest request,@PathVariable String userId,@PathVariable String operator) {
        return imSystemService.destroyUserByAdmin(request,userId,operator);
    }

    //客户端
    @PostMapping("/destroyUser")
    public ResponseVO destroyUser(@RequestBody DestroyUserReq req) {
        return imSystemService.destroyUser(req);
    }

    @PostMapping("/getLog")
    public ResponseVO getLog(@RequestBody LogReq req) {
        return imSystemService.getLog(req);
    }

}
