package com.lld.im.service.system.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.system.model.req.*;
import com.lld.im.service.system.service.ImSystemNotificationService;
import com.lld.im.service.system.service.ImSystemParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping("v1/system/param")
public class SystemParamController {


    @Autowired
    ImSystemParamService imSystemParamService;

    @PostMapping("/getByPage")
    public ResponseVO getParams(@RequestBody GetParamsReq req){
        return imSystemParamService.getParams(req);
    }

    @PostMapping("/create")
    public ResponseVO createParam(HttpServletRequest request,@RequestBody ParamsReq req){
        return imSystemParamService.createParam(request,req);
    }


    @PutMapping("/update")
    public ResponseVO updateParam(HttpServletRequest request,@RequestBody ModifyParamsReq req){
        return imSystemParamService.updateParam(request,req);
    }


    @DeleteMapping("/delete/{id}/{appId}/{operator}")
    public ResponseVO deleteParam(HttpServletRequest request,@PathVariable Integer id ,@PathVariable Integer appId ,@PathVariable String operator){
        return imSystemParamService.deleteParam(request,id,appId,operator);
    }
}
