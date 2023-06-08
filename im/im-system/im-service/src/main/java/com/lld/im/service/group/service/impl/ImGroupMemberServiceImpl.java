package com.lld.im.service.group.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.group.AddGroupMemberPack;
import com.lld.im.codec.pack.group.GroupMemberSpeakPack;
import com.lld.im.codec.pack.group.RemoveGroupMemberPack;
import com.lld.im.codec.pack.group.UpdateGroupMemberPack;
import com.lld.im.common.ResponseVO;

import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.*;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.lld.im.service.group.model.callback.AddMemberAfterCallback;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.AddMemberResp;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupRequestService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.resp.GetSingleUserInfoResp;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.GroupMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
public class ImGroupMemberServiceImpl extends ServiceImpl<ImGroupMemberMapper, ImGroupMemberEntity> implements ImGroupMemberService {

    @Autowired
    ImGroupMemberMapper imGroupMemberMapper;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService groupMemberService;

    @Autowired
    ImGroupRequestService imGroupRequestService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Autowired
    RedisTemplate redisTemplate;

//
    @Override
    public ResponseVO importGroupMember(ImportGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        for (GroupMemberDto memberId : req.getMembers()) {
            ResponseVO responseVO = null;
            try {
                responseVO = groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberId);
            } catch (Exception e) {
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse();
            }
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            if (responseVO.isOk()) {
                addMemberResp.setResult(0);
            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                addMemberResp.setResult(2);
            } else {
                addMemberResp.setResult(1);
            }
            resp.add(addMemberResp);
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto) {

        ResponseVO<GetSingleUserInfoResp> singleUserInfo = imUserService.getSingleUserInfo(dto.getMemberId(), appId);
        if(!singleUserInfo.isOk()){
            return singleUserInfo;
        }
        if (dto.getRole() != null && GroupMemberRoleEnum.OWNER.getCode() == dto.getRole()) {
            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id", groupId);
            queryOwner.eq("app_id", appId);
            queryOwner.eq("role", GroupMemberRoleEnum.OWNER.getCode());
            Integer ownerNum = imGroupMemberMapper.selectCount(queryOwner);
            if (ownerNum > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        query.eq("app_id", appId);
        query.eq("member_id", dto.getMemberId());
        ImGroupMemberEntity memberDto = imGroupMemberMapper.selectOne(query);

        if (memberDto == null) {
            //初次加群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setGroupId(groupId);
            memberDto.setAppId(appId);
            int insert = imGroupMemberMapper.insert(memberDto);
            if (insert == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        } else if (GroupMemberRoleEnum.LEAVE.getCode() == memberDto.getRole()) {
            //重新进群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setJoinTime(new Timestamp(new Date().getTime()));
            memberDto.setMute(0);
            memberDto.setSpeakDate(null);
            memberDto.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
            int update = imGroupMemberMapper.update(memberDto, query);
            if (update == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);

    }


    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId) {

        GetRoleInGroupResp resp = new GetRoleInGroupResp();

        QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
        queryOwner.eq("group_id", groupId);
        queryOwner.eq("app_id", appId);
        queryOwner.eq("member_id", memberId);

        ImGroupMemberEntity imGroupMemberEntity = imGroupMemberMapper.selectOne(queryOwner);
        if (imGroupMemberEntity == null || imGroupMemberEntity.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }

        resp.setSpeakDate(imGroupMemberEntity.getSpeakDate());
        resp.setGroupMemberId(imGroupMemberEntity.getGroupMemberId());
        resp.setMemberId(imGroupMemberEntity.getMemberId());
        resp.setRole(imGroupMemberEntity.getRole());
        resp.setMute(imGroupMemberEntity.getMute());
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req) {

//        if (req.getLimit() != null) {
//            Page<ImGroupMemberEntity> objectPage = new Page<>(req.getOffset(), req.getLimit());
//            QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
//            query.eq("app_id", req.getAppId());
//            query.eq("member_id", req.getMemberId());
//            IPage<ImGroupMemberEntity> imGroupMemberEntityPage = imGroupMemberMapper.selectPage(objectPage, query);
//
//            Set<String> groupId = new HashSet<>();
//            List<ImGroupMemberEntity> records = imGroupMemberEntityPage.getRecords();
//            records.forEach(e -> {
//                groupId.add(e.getGroupId());
//            });
//
//            return ResponseVO.successResponse(groupId);
//        } else {
//            return ResponseVO.successResponse(imGroupMemberMapper.getJoinedGroupId(req.getAppId(), req.getMemberId()));
//        }
        return ResponseVO.successResponse(imGroupMemberMapper.getJoinedGroupId(req.getAppId(), req.getMemberId()));

    }

    /**
     * 添加群成员，拉人入群的逻辑，直接进入群聊。如果是后台管理员，则直接拉入群，
     * 否则只有私有群可以调用本接口，并且群成员也可以拉人入群.只有私有群可以调用本接口
     */
    @Override
    public ResponseVO addMember(AddGroupMemberReq req) {

        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        List<String> members = req.getMembers();
        List<String> successId = new ArrayList<>();
        List<AddMemberResp> resp = new ArrayList<>();


        if(appConfig.isAddGroupMemberBeforeCallback()){

            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.GroupMemberAddBefore, JSONObject.toJSONString(req));
            if(!responseVO.isOk()) {
                storeLog(req.getAppId(),req.getOperator(),0,"添加群成员");
                return responseVO;
            }
        }

        ImGroupEntity group = groupResp.getData();


        /**
         * 私有群（private）	类似普通微信群，创建后仅支持已在群内的好友邀请加群，且无需被邀请方同意或群主审批
         * 公开群（Public）	类似 QQ 群，创建后群主可以指定群管理员，需要群主或管理员审批通过才能入群
         * 群类型 1私有群（类似微信） 2公开群(类似qq）
         *
         */

//        if (!isAdmin && GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
//            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
//        }


        if(group.getApplyJoinType() == 0){
            storeLog(req.getAppId(),req.getOperator(),0,"添加群成员");
            return ResponseVO.errorResponse(GroupErrorCode.FORBID_ADD_GROUP.getCode(),GroupErrorCode.FORBID_ADD_GROUP.getError());
        }
        if(req.getIsManager() == 1 && group.getApplyJoinType() == 1 ){
            this.addMembers(members,req,group,successId,resp);
        }else if( req.getIsManager() != 1 && group.getApplyJoinType() == 1 ){
            req.getMembers().forEach(e->{
                GroupMemberDto memberDto = new GroupMemberDto();
                if(req.getIsInvite() == 1){
                    memberDto.setJoinType(GroupJoinTypeEnum.MEMBER_INVITE.getValue());
                }else {
                    memberDto.setJoinType(GroupJoinTypeEnum.ACCOUNT_SEARCH.getValue());
                }
                memberDto.setMemberId(e);
                memberDto.setAppId(req.getAppId());
                memberDto.setOperator(req.getOperator());
                if(StringUtils.isNotEmpty(req.getAddWording())){
                    memberDto.setAddWording(req.getAddWording());
                }
                imGroupRequestService.addGroupRequest(req.getGroupId(),memberDto,req.getAppId());
            });
        }else if(group.getApplyJoinType() == 2 ){
            this.addMembers(members,req,group,successId,resp);
        }

        AddGroupMemberPack addGroupMemberPack = new AddGroupMemberPack();
        addGroupMemberPack.setGroupId(req.getGroupId());
        addGroupMemberPack.setMembers(successId);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.ADDED_MEMBER, addGroupMemberPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        if(appConfig.isAddGroupMemberAfterCallback()){
            AddMemberAfterCallback dto = new AddMemberAfterCallback();
            dto.setGroupId(req.getGroupId());
            dto.setGroupType(group.getGroupType());
            dto.setMemberId(resp);
            dto.setOperator(req.getOperator());
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.GroupMemberAddAfter, JSONObject.toJSONString(dto));
        }
        storeLog(req.getAppId(),req.getOperator(),1,"添加群成员");
        return ResponseVO.successResponse(resp);
    }

    public void   addMembers(List<String> members,AddGroupMemberReq req,ImGroupEntity group,List<String> successId,List<AddMemberResp> resp){
        for (String memberId : members) {
            ResponseVO responseVO;
            GroupMemberDto memberDto = new GroupMemberDto();
            memberDto.setMemberId(memberId);
            memberDto.setJoinType(GroupJoinTypeEnum.MEMBER_INVITE.getValue());
            memberDto.setAppId(req.getAppId());
            memberDto.setJoinTime(new Date());
            memberDto.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
            try {
                responseVO = groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberDto);
            } catch (Exception e) {
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse();
            }
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId);
            if (responseVO.isOk()) {
                successId.add(memberId);
                addMemberResp.setResult(0);
            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                addMemberResp.setResult(2);
                addMemberResp.setResultMessage(responseVO.getMsg());
            } else {
                addMemberResp.setResult(1);
                addMemberResp.setResultMessage(responseVO.getMsg());
            }
            resp.add(addMemberResp);
        }
    }

//    public void   addMemberRequest (List<String> members){
//        for (String memberId : members) {
//            ResponseVO responseVO;
//            GroupMemberDto memberDto = new GroupMemberDto();
//            memberDto.setMemberId(memberId);
//            memberDto.setJoinType(GroupJoinTypeEnum.MEMBER_INVITE.getValue());
//            memberDto.setAppId(req.getAppId());
//            memberDto.setJoinTime(new Date());
//            memberDto.setMute(group.getMute());
//            try {
//                responseVO = groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberDto);
//            } catch (Exception e) {
//                e.printStackTrace();
//                responseVO = ResponseVO.errorResponse();
//            }
//            AddMemberResp addMemberResp = new AddMemberResp();
//            addMemberResp.setMemberId(memberId);
//            if (responseVO.isOk()) {
//                successId.add(memberId);
//                addMemberResp.setResult(0);
//            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
//                addMemberResp.setResult(2);
//                addMemberResp.setResultMessage(responseVO.getMsg());
//            } else {
//                addMemberResp.setResult(1);
//                addMemberResp.setResultMessage(responseVO.getMsg());
//            }
//            resp.add(addMemberResp);
//        }
//    }

    @Override
    public ResponseVO removeMember(RemoveGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();
        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            storeLog(req.getAppId(),req.getOperator(),0,"移除群成员");
            return groupResp;
        }

        ImGroupEntity group = groupResp.getData();

        if (!isAdmin) {

            //获取操作人的权限 是管理员or群主or群成员
            ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());
            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode();

            if (!isOwner && !isManager) {
                storeLog(req.getAppId(),req.getOperator(),0,"移除群成员");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            //私有群必须是群主才能踢人
            if (!isOwner && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()) {
                storeLog(req.getAppId(),req.getOperator(),0,"移除群成员");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }

            //公开群管理员和群主可踢人，但管理员只能踢普通群成员
            if (GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
//                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                //获取被踢人的权限
                ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
                if (!roleInGroupOne.isOk()) {
                    return roleInGroupOne;
                }
                GetRoleInGroupResp memberRole = roleInGroupOne.getData();
                if (memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                    storeLog(req.getAppId(),req.getOperator(),0,"移除群成员");
                    throw new ApplicationException(GroupErrorCode.GROUP_OWNER_IS_NOT_REMOVE);
                }
                //是管理员并且被踢人不是群成员，无法操作
                if (isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                    storeLog(req.getAppId(),req.getOperator(),0,"移除群成员");
                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }
            }
        }
        ResponseVO responseVO = groupMemberService.removeGroupMember(req.getGroupId(), req.getAppId(), req.getMemberId());
        if(responseVO.isOk()) {

            RemoveGroupMemberPack removeGroupMemberPack = new RemoveGroupMemberPack();
            removeGroupMemberPack.setGroupId(req.getGroupId());
            removeGroupMemberPack.setMember(req.getMemberId());
            groupMessageProducer.producer(req.getMemberId(), GroupEventCommand.DELETED_MEMBER, removeGroupMemberPack
                    , new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

            if (appConfig.isDeleteGroupMemberAfterCallback()) {
                callbackService.callback(req.getAppId(),
                        Constants.CallbackCommand.GroupMemberDeleteAfter,
                        JSONObject.toJSONString(req));
            }
        }
        storeLog(req.getAppId(),req.getOperator(),1,"移除群成员");
        return responseVO;
    }


    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 删除群成员，内部调用
     * @author lld
     */
    @Override
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId) {

        ResponseVO<GetSingleUserInfoResp> singleUserInfo = imUserService.getSingleUserInfo(memberId, appId);
        if(!singleUserInfo.isOk()){
            return singleUserInfo;
        }

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = getRoleInGroupOne(groupId, memberId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        GetRoleInGroupResp data = roleInGroupOne.getData();
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.LEAVE.getCode());
        imGroupMemberEntity.setLeaveTime(new Timestamp(new Date().getTime()));
        imGroupMemberEntity.setGroupMemberId(data.getGroupMemberId());
        imGroupMemberMapper.updateById(imGroupMemberEntity);
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO exitGroup(ExitGroupReq req) {
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()){
            storeLog(req.getAppId(),req.getOperator(),0,"退出群聊");
            return group;
        }
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!roleInGroupOne.isOk()){
            storeLog(req.getAppId(),req.getOperator(),0,"退出群聊");
            return roleInGroupOne;
        }

        ImGroupMemberEntity update = new ImGroupMemberEntity();
        update.setRole(GroupMemberRoleEnum.LEAVE.getCode());
        update.setLeaveTime(new Timestamp(new Date().getTime()));

        QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
        queryOwner.eq("group_id", req.getGroupId());
        queryOwner.eq("app_id", req.getAppId());
        queryOwner.eq("member_id", req.getOperator());

        imGroupMemberMapper.update(update,queryOwner);
        storeLog(req.getAppId(),req.getOperator(),1,"退出群聊");
        return ResponseVO.successResponse();
    }


    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId) {
        List<GroupMemberDto> groupMember = imGroupMemberMapper.getGroupMember(appId, groupId);
        groupMember.forEach(e->{
            if(e.getMute() == 1 && ObjectUtils.isNotEmpty(e.getSpeakDate())){
                if(e.getSpeakDate().before(new Date())){
                    e.setSpeakDate(null);
                    e.setMute(0);
                    ImGroupMemberEntity groupMemberByMemberId = getGroupMemberByMemberId(groupId, appId, e.getMemberId());
                    groupMemberByMemberId.setSpeakDate(null);
                    groupMemberByMemberId.setMute(0);
                    imGroupMemberMapper.updateById(groupMemberByMemberId);
                }
            }
        });
        return ResponseVO.successResponse(groupMember);
    }

    @Override
    public List<String> getGroupMemberId(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupMemberId(appId, groupId);
    }

    @Override
    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupManager(groupId, appId);
    }

    @Override
    public ImGroupMemberEntity getGroupMemberByMemberId(String groupId, Integer appId,String memberId) {
        LambdaQueryWrapper<ImGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMemberEntity::getGroupId,groupId);
        wrapper.eq(ImGroupMemberEntity::getAppId,appId);
        wrapper.eq(ImGroupMemberEntity::getMemberId,memberId);
        wrapper.ne(ImGroupMemberEntity::getRole,GroupMemberRoleEnum.LEAVE.getCode());
        return imGroupMemberMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req) {

        boolean isAdmin = false;

        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
            return group;
        }

        ImGroupEntity groupData = group.getData();
        if (groupData.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        //是否是自己修改自己的资料
        boolean isMeOperate = req.getOperator().equals(req.getMemberId());

        if (!isAdmin) {
            //昵称只能自己修改 权限只能群主或管理员修改
            if (StringUtils.isNotBlank(req.getAlias()) && !isMeOperate) {
                storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_ONESELF);
            }
            //私有群不能设置管理员
            if (groupData.getGroupType() == GroupTypeEnum.PRIVATE.getCode() &&
                    req.getRole() != null && (req.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() ||
                    req.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
                storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
            }

            //如果要修改权限相关的则走下面的逻辑
            if(req.getRole() != null){
                //获取被操作人的是否在群内
                ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
                if(!roleInGroupOne.isOk()){
                    storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
                    return roleInGroupOne;
                }

                //获取操作人权限
                ResponseVO<GetRoleInGroupResp> operateRoleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());
                if(!operateRoleInGroupOne.isOk()){
                    storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
                    return operateRoleInGroupOne;
                }

                GetRoleInGroupResp data = operateRoleInGroupOne.getData();
                Integer roleInfo = data.getRole();
                boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode();

                //不是管理员不能修改权限
                if(req.getRole() != null && !isOwner && !isManager){
                    storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                }

                //管理员只有群主能够设置
                if(req.getRole() != null && req.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() && !isOwner){
                    storeLog(req.getAppId(),req.getOperator(),0,"修改群成员信息");
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }

            }
        }

        ImGroupMemberEntity update = new ImGroupMemberEntity();

        if (StringUtils.isNotBlank(req.getAlias()) && ObjectUtils.isEmpty(req.getRole()) && ObjectUtils.isEmpty(req.getMute())) {
            update.setAlias(req.getAlias());
            UpdateWrapper<ImGroupMemberEntity> objectUpdateWrapper = new UpdateWrapper<>();
            objectUpdateWrapper.eq("app_id", req.getAppId());
            objectUpdateWrapper.eq("member_id", req.getMemberId());
            objectUpdateWrapper.eq("group_id", req.getGroupId());
            imGroupMemberMapper.update(update, objectUpdateWrapper);
            storeLog(req.getAppId(),req.getOperator(),1,"修改群成员信息");
            return ResponseVO.successResponse();
        }else if (StringUtils.isNotBlank(req.getAlias())){
            update.setAlias(req.getAlias());
        }

        //不能直接修改为群主
        if(req.getRole() != null && req.getRole() != GroupMemberRoleEnum.OWNER.getCode()){
            update.setRole(req.getRole());
        }

        UpdateWrapper<ImGroupMemberEntity> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("app_id", req.getAppId());
        objectUpdateWrapper.eq("member_id", req.getMemberId());
        objectUpdateWrapper.eq("group_id", req.getGroupId());
        imGroupMemberMapper.update(update, objectUpdateWrapper);

        UpdateGroupMemberPack pack = new UpdateGroupMemberPack();
        BeanUtils.copyProperties(req, pack);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.UPDATED_MEMBER, pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        storeLog(req.getAppId(),req.getOperator(),1,"修改群成员信息");
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId) {

        //更新旧群主
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", appId);
        updateWrapper.eq("group_id", groupId);
        updateWrapper.eq("role", GroupMemberRoleEnum.OWNER.getCode());
        imGroupMemberMapper.update(imGroupMemberEntity, updateWrapper);

        //更新新群主
        ImGroupMemberEntity newOwner = new ImGroupMemberEntity();
        newOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        UpdateWrapper<ImGroupMemberEntity> ownerWrapper = new UpdateWrapper<>();
        ownerWrapper.eq("app_id", appId);
        ownerWrapper.eq("group_id", groupId);
        ownerWrapper.eq("member_id", owner);
        imGroupMemberMapper.update(newOwner, ownerWrapper);
        storeLog(appId,owner,1,"群主转让");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO speak(SpeakMemberReq req) {

        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            storeLog(req.getAppId(),req.getOperator(),0,"禁言群成员");
            return groupResp;
        }

        boolean isAdmin = false;
        boolean isOwner;
        boolean isManager;
        GetRoleInGroupResp memberRole = null;

        if (!isAdmin) {

            //获取操作人的权限 是管理员or群主or群成员
            ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());
            if (!role.isOk()) {
                storeLog(req.getAppId(),req.getOperator(),0,"禁言群成员");
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
            isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode();

            if (!isOwner && !isManager) {
                storeLog(req.getAppId(),req.getOperator(),0,"禁言群成员");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            //获取被操作的权限
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }
            memberRole = roleInGroupOne.getData();
            //被操作人是群主只能app管理员操作
            if (memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                storeLog(req.getAppId(),req.getOperator(),0,"禁言群成员");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
            }

            //是管理员并且被操作人不是群成员，无法操作
            if (isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                storeLog(req.getAppId(),req.getOperator(),0,"禁言群成员");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        if(memberRole == null){
            //获取被操作的权限
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }
            memberRole = roleInGroupOne.getData();
        }

        imGroupMemberEntity.setGroupMemberId(memberRole.getGroupMemberId());
        imGroupMemberEntity.setMute(req.getMute());
        if(req.getMute() == 0){
            imGroupMemberEntity.setSpeakDate(null);
        }else{
            imGroupMemberEntity.setSpeakDate(req.getSpeakDate());
        }
        int i = imGroupMemberMapper.updateById(imGroupMemberEntity);
        if(i == 1){
            GroupMemberSpeakPack pack = new GroupMemberSpeakPack();
            BeanUtils.copyProperties(req,pack);
            groupMessageProducer.producer(req.getOperator(),GroupEventCommand.SPEAK_GROUP_MEMBER,pack, new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        }
        storeLog(req.getAppId(),req.getOperator(),1,"禁言群成员");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<Collection<String>> syncMemberJoinedGroup(String operater, Integer appId) {
        return ResponseVO.successResponse(imGroupMemberMapper.syncJoinedGroupId(appId,operater,GroupMemberRoleEnum.LEAVE.getCode()));
    }

    private void storeLog(Integer appId, String userId ,int status,String text) {
        SysLog sysLog = new SysLog();
        Object obj = redisTemplate.opsForHash().get(appId + Constants.RedisConstants.UserSessionConstants + userId, "1:windows");
        JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
        sysLog.setUserId(object.getString("userId"));
        sysLog.setImei(object.getString("imei"));
        sysLog.setAppId(Integer.valueOf(object.getString("appId")));
        sysLog.setBrokerHost(object.getString("brokerHost"));
        sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
        sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));
        sysLog.setClientType(Integer.valueOf(object.getString("clientType")));
        sysLog.setStatus(status);
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
    }


}
