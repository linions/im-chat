package com.lld.im.service.group.controller;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("v1/group/member")
public class ImGroupMemberController {

    @Autowired
    ImGroupMemberService groupMemberService;

    @PostMapping("/importGroupMember")
    public ResponseVO importGroupMember(@RequestBody @Validated ImportGroupMemberReq req)  {
        return groupMemberService.importGroupMember(req);
    }
    @GetMapping("getMemberById/{groupId}/{appId}/{memberId}")
    public ResponseVO getGroupMemberByMemberId(@PathVariable String groupId, @PathVariable Integer appId, @PathVariable String memberId){
        return ResponseVO.successResponse(groupMemberService.getGroupMemberByMemberId(groupId, appId, memberId));
    }

    @PostMapping("/add")
    public ResponseVO addMember(@RequestBody @Validated AddGroupMemberReq req)  {
        return groupMemberService.addMember(req);
    }

    @RequestMapping("/remove")
    public ResponseVO removeMember(@RequestBody @Validated RemoveGroupMemberReq req)  {
        return groupMemberService.removeMember(req);
    }

    @PostMapping("/exit")
    public ResponseVO exitGroup(@RequestBody @Validated ExitGroupReq req)  {
        return groupMemberService.exitGroup(req);
    }

    @PostMapping("/update")
    public ResponseVO updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req)  {
        return groupMemberService.updateGroupMember(req);
    }

    @PostMapping("/speak")
    public ResponseVO speak(@RequestBody @Validated SpeakMemberReq req)  {
        return groupMemberService.speak(req);
    }

}
