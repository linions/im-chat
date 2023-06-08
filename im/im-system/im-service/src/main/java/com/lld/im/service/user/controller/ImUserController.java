package com.lld.im.service.user.controller;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lld.im.common.ClientType;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.RouteInfo;
import com.lld.im.common.utils.HttpRequestUtils;
import com.lld.im.common.utils.RouteInfoParseUtil;
import com.lld.im.common.utils.SigAPI;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.model.resp.LoginResp;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.user.service.ImUserStatusService;
import com.lld.im.service.utils.ZKit;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.lang.model.type.ErrorType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private RouteHandle routeHandle;

    @Autowired
    private ZKit zKit;

    @Autowired
    private ImUserStatusService imUserStatusService;


    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req){
        return imUserService.importUser(req);
    }

    @PostMapping("/getUserByPage")
    public ResponseVO getUserByPage(@RequestBody UserReq req){
        return imUserService.getUserByPage(req);
    }


    @PostMapping("/getCode")
    public ResponseVO getLoginCode(@RequestBody GetCodeReq req) {
        return imUserService.getLoginCode(req);
    }


    @PostMapping("/login")
    @Transactional
    public ResponseVO login(@RequestBody LoginReq req) throws InterruptedException {
        LoginResp loginResp = new LoginResp();
        ResponseVO<ImUserDataEntity> login = imUserService.login(req);
        if (login.isOk()){
            BeanUtils.copyProperties(login.getData(),loginResp);

            List<String> nodeList;
            if(req.getClientType() == ClientType.WEB.getCode()){
                nodeList = zKit.getAllWebNode();
            }else {
                nodeList = zKit.getAllTcpNode();
            }
            //ip:port
            String s = routeHandle.routeServer(nodeList, loginResp.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            loginResp.setUserSign(SigAPI.getSign(loginResp.getUserId(),req.getAppId()));
            loginResp.setRouteInfo(parse);
            return ResponseVO.successResponse(loginResp);
        }
        return ResponseVO.errorResponse(500,"登录失败");
    }

    @PostMapping("/logOut")
    public ResponseVO logOut(@RequestBody LogoutReq req){
        return imUserService.logout(req);
    }

    @PostMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated GetUserSequenceReq req) {
        return imUserService.getUserSequence(req);
    }

    @PostMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated SubscribeUserOnlineStatusReq req) {
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    @PostMapping("/setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated SetUserCustomerStatusReq req) {
        imUserStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }

    @PostMapping("/queryFriendOnlineStatus")
    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated PullFriendOnlineStatusReq req) {
        return ResponseVO.successResponse(imUserStatusService.queryFriendOnlineStatus(req));
    }

    @PostMapping("/queryUserOnlineStatus")
    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated PullUserOnlineStatusReq req) {
        return ResponseVO.successResponse(imUserStatusService.queryUserOnlineStatus(req));
    }

    //  上传用户头像
    @PostMapping("/uploadLogo/{userId}/{appId}")
    public ResponseVO uploadHospLogo(HttpServletRequest request,@RequestParam("photo") MultipartFile uploadFile ,@PathVariable String userId,@PathVariable Integer appId ) throws IOException {
            return imUserService.uploadLogo(request,uploadFile,userId,appId);
    }


    @PostMapping("/getUserPercentage/{appId}")
    public ResponseVO getUserPercentage(@PathVariable Integer appId) {
        return imUserService.getUserPercentage(appId);
    }

//    折线图数据
    @GetMapping("/getDataOfWeek/{appId}")
    public ResponseVO getDataOfWeek(@PathVariable Integer appId) {
        return imUserService.getDataOfWeek(appId);
    }

    @PostMapping("/adminLogin")
    public ResponseVO adminLogin(HttpServletRequest request, @RequestBody AdminLoginReq req) {
        return imUserService.adminLogin(request,req);
    }

    @PostMapping("/adminLogout/{userId}")
    public ResponseVO adminLogout(HttpServletRequest request,@PathVariable String userId) {
        return imUserService.adminLogout(request,userId);
    }

    @PutMapping("/changeStatus/{userId}/{status}/{operator}")
    public ResponseVO updateUserStatus(HttpServletRequest request,@PathVariable String userId,@PathVariable int status,@PathVariable String operator)  {
        return imUserService.updateUserStatus(request,userId,status,operator);
    }

}
