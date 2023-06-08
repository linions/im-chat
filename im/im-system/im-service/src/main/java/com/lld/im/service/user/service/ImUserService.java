package com.lld.im.service.user.service;

import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.model.resp.GetSingleUserInfoResp;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ImUserService extends IService<ImUserDataEntity> {

    ResponseVO importUser(ImportUserReq importUserReq);

    ResponseVO getUserInfo(GetUserInfoReq req);

    ResponseVO<GetSingleUserInfoResp> getSingleUserInfo(String userId, Integer appId);

    ResponseVO modifyUserInfo(HttpServletRequest request,ModifyUserInfoReq req);


    ResponseVO<ImUserDataEntity> login(LoginReq req);

    ResponseVO getUserSequence(GetUserSequenceReq req);

    ResponseVO getLoginCode(GetCodeReq req);

    ResponseVO findPassword(FindPasswordReq req);

    ResponseVO getCertify(GetCertifyReq req);

    ResponseVO logout(LogoutReq req);

    ResponseVO getUserByPage(UserReq req);

    ResponseVO getUserPercentage(Integer appId);

    ResponseVO getDataOfWeek(Integer appId);

    ResponseVO adminLogin(HttpServletRequest request, AdminLoginReq req);

    ResponseVO adminLogout(HttpServletRequest request, String userId);

    ResponseVO updateUserStatus(HttpServletRequest request,String userId, int status,String operator);

    ResponseVO createUser(HttpServletRequest request, ModifyUserInfoReq req);

    ResponseVO uploadLogo(HttpServletRequest request,MultipartFile uploadFile, String userId, Integer appId) throws IOException;
}
