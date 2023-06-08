package com.lld.im.service.group.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.service.GroupMessageService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("v1/group")
public class ImGroupController {

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    GroupMessageService groupMessageService;



    @PostMapping("/importGroup")
    public ResponseVO importGroup(@RequestBody @Validated ImportGroupReq req)  {
        return imGroupService.importGroup(req);
    }

    @PostMapping("/searchGroup")
    public ResponseVO searchGroup(@RequestBody @Validated SearchGroupReq req)  {
        return imGroupService.searchGroup(req);
    }

    @PostMapping("/createGroup")
    public ResponseVO createGroup(@RequestBody @Validated CreateGroupReq req)  {
        return imGroupService.createGroup(req);
    }

    @PostMapping("/getGroupInfo")
    public ResponseVO getGroupInfo(@RequestBody @Validated GetGroupReq req)  {
        return imGroupService.getGroup(req);
    }

    @PostMapping("/updateGroup")
    public ResponseVO update(@RequestBody @Validated UpdateGroupReq req)  {
        return imGroupService.updateBaseGroupInfo(req);
    }

    @PostMapping("/getJoinedGroup")
    public ResponseVO getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req)  {
        return imGroupService.getJoinedGroup(req);
    }


    @PostMapping("/destroyGroup")
    public ResponseVO destroyGroup(@RequestBody @Validated DestroyGroupReq req)  {
        return imGroupService.destroyGroup(req);
    }

    @PostMapping("/transferGroup")
    public ResponseVO transferGroup(@RequestBody @Validated TransferGroupReq req)  {
        return imGroupService.transferGroup(req);
    }

    @PostMapping("/forbidSendMessage")
    public ResponseVO forbidSendMessage(@RequestBody @Validated MuteGroupReq req)  {
        return imGroupService.muteGroup(req);
    }

    //管理员使用 发送消息
    @PostMapping("/sendMessage")
    public ResponseVO sendMessage(@RequestBody @Validated SendGroupMessageReq req)  {
        return ResponseVO.successResponse(groupMessageService.send(req));
    }

    @PostMapping("/syncJoinedGroup")
    public ResponseVO syncJoinedGroup(@RequestBody @Validated SyncReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        return imGroupService.syncJoinedGroupList(req);
    }

    //    获取聊天记录
    @PostMapping("/getGroupChatMessage")
    public ResponseVO getGroupChatMessage(@RequestBody @Validated GetGroupMessageReq req)  {
        return groupMessageService.getChatMessage(req);
    }

    @PostMapping("/uploadLogo/{groupId}/{operator}")
    public ResponseVO uploadHospLogo(HttpServletRequest request,@RequestParam("photo") MultipartFile uploadFile,@PathVariable String groupId,@PathVariable String operator) throws IOException {
        return imGroupService.uploadLogo(request,uploadFile,groupId,operator);
    }


}
