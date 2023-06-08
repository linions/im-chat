package com.lld.im.service.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.system.dao.ImSysNotificationEntity;
import com.lld.im.service.system.dao.ImSysParamEntity;
import com.lld.im.service.system.model.req.GetParamsReq;
import com.lld.im.service.system.model.req.ModifyParamsReq;
import com.lld.im.service.system.model.req.ParamsReq;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;


@Service
public interface ImSystemParamService extends IService<ImSysParamEntity> {

    ResponseVO getParams(GetParamsReq req);

    ResponseVO createParam(HttpServletRequest request, ParamsReq req);

    ResponseVO updateParam(HttpServletRequest request, ModifyParamsReq req);

    ResponseVO deleteParam(HttpServletRequest request, Integer id, Integer appId, String operator);
}
