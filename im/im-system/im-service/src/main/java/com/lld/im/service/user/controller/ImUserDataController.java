package com.lld.im.service.user.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.service.ImUserService;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @className: ImUserDataController
 * @author: linion
 * @date: 2023/3/12 17:35
 * @version: 1.0
 */

@RestController
@CrossOrigin
@RequestMapping("v1/user/data")
public class ImUserDataController {

    private static Logger logger = LoggerFactory.getLogger(ImUserDataController.class);

    @Autowired
    ImUserService imUserService;

    @PostMapping("/getUserInfo")
    public ResponseVO getUserInfo(@RequestBody GetUserInfoReq req){//@Validated
        return imUserService.getUserInfo(req);
    }

    //管理员创建账户
    @PostMapping("/create")
    public ResponseVO createUser(HttpServletRequest request, @RequestBody ModifyUserInfoReq req){//@Validated
        return imUserService.createUser(request,req);
    }


    @PostMapping("/getSingleUserInfo")
    public ResponseVO getSingleUserInfo(@RequestBody @Validated UserId req){
        return imUserService.getSingleUserInfo(req.getUserId(),req.getAppId());
    }

    @PutMapping("/modifyUserInfo")
    public ResponseVO modifyUserInfo(HttpServletRequest request, @RequestBody @Validated ModifyUserInfoReq req){
        return imUserService.modifyUserInfo(request,req);
    }

    @PostMapping("/findPassword")
    public ResponseVO findPassword(@RequestBody @Validated FindPasswordReq req){
        return imUserService.findPassword(req);
    }

    @PostMapping("/getCertify")
    public ResponseVO getCertify(@RequestBody GetCertifyReq req){
        return imUserService.getCertify(req);
    }
}