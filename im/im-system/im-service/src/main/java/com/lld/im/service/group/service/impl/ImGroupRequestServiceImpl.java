package com.lld.im.service.group.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.friendship.ApproveFriendRequestPack;
import com.lld.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.lld.im.codec.pack.group.ApproveGroupRequestPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.*;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.dao.ImGroupRequestEntity;
import com.lld.im.service.group.dao.mapper.ImGroupRequestMapper;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupRequestService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.req.GetUserInfoReq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Service
public class ImGroupRequestServiceImpl extends ServiceImpl<ImGroupRequestMapper, ImGroupRequestEntity> implements ImGroupRequestService {

    @Autowired
    ImGroupRequestMapper imGroupRequestMapper;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Autowired
    RedisTemplate redisTemplate;



    //A + B
    @Override
    public ResponseVO addGroupRequest(String groupId, GroupMemberDto dto, Integer appId) {

        QueryWrapper<ImGroupRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId);
        queryWrapper.eq("group_id",groupId);
        queryWrapper.eq("Member_id",dto.getMemberId());
        ImGroupRequestEntity request = imGroupRequestMapper.selectOne(queryWrapper);

        long seq = redisSeq.doGetSeq(appId+":"+ Constants.SeqConstants.GroupRequest);

        if(ObjectUtils.isEmpty(request)){
            request = new ImGroupRequestEntity();
            request.setAddSource(dto.getJoinType());
            request.setGroupId(groupId);
            request.setAddWording(dto.getAddWording());
            request.setSequence(seq);
            request.setAppId(appId);
            request.setMemberId(dto.getMemberId());
            request.setOperatorId(dto.getOperator());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            imGroupRequestMapper.insert(request);

        }else {
            if(StringUtils.isNotBlank(dto.getOperator())){
                request.setOperatorId(dto.getOperator());
            }
            //修改记录内容 和更新时间
            if(StringUtils.isNotBlank(dto.getJoinType())){
                request.setAddSource(dto.getJoinType());
            }
            if(StringUtils.isNotBlank(dto.getAddWording())){
                request.setAddWording(dto.getAddWording());
            }
            request.setSequence(seq);
            request.setApproveStatus(0);
            request.setReadStatus(0);
            imGroupRequestMapper.updateById(request);
        }

        writeUserSeq.writeUserSeq(appId,groupId,Constants.SeqConstants.GroupRequest,seq);

        //发送好友申请的tcp给群管理员
        ResponseVO<List<GroupMemberDto>> groupMember = imGroupMemberService.getGroupMember(groupId, appId);
        if(groupMember.isOk() && CollectionUtils.isNotEmpty(groupMember.getData())){
            ImGroupRequestEntity finalRequest = request;
            groupMember.getData().forEach(e->{
                boolean isManager = equals(e.getRole()) || GroupMemberRoleEnum.OWNER.equals(e.getRole());
                if(isManager){
                    messageProducer.sendToUser(e.getMemberId(),null, "", GroupEventCommand.JOIN_GROUP, finalRequest, appId);
                }
            });
        }
        return ResponseVO.successResponse("好友申请已发送");
    }





    @Override
    @Transactional
    public ResponseVO approveGroupRequest(ApproveGroupRequestReq req) {
        ImGroupRequestEntity imGroupRequestEntity= imGroupRequestMapper.selectById(req.getId());
        if(imGroupRequestEntity == null){
            storeLog(req.getAppId(),req.getOperator(),0,"审批群申请");
            throw new ApplicationException(GroupErrorCode.GROUP_REQUEST_IS_NOT_EXIST);
        }

        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(imGroupRequestEntity.getGroupId(), req.getAppId());
        ImGroupEntity groupData = group.getData();
        if(!group.isOk()){
            storeLog(req.getAppId(),req.getOperator(),0,"审批群申请");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(imGroupRequestEntity.getGroupId(), req.getOperator(), req.getAppId());
        if(!roleInGroupOne.isOk() || roleInGroupOne.getData().getRole() == GroupMemberRoleEnum.ORDINARY.getCode()){
            storeLog(req.getAppId(),req.getOperator(),0,"审批群申请");
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        long seq = redisSeq.doGetSeq(req.getAppId()+":"+ Constants.SeqConstants.GroupRequest);



        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperator(),Constants.SeqConstants.FriendshipRequest,seq);

        if(ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()){

            if(GroupJoinTypeEnum.FORBID_ANY_MEMBER.equals(groupData.getApplyJoinType())){
                storeLog(req.getAppId(),req.getOperator(),0,"审批群申请");
                throw new ApplicationException(GroupErrorCode.FORBID_ADD_GROUP);
            }



            QueryWrapper<ImGroupMemberEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id",req.getAppId());
            queryWrapper.eq("group_id",imGroupRequestEntity.getGroupId());
            int count = imGroupMemberService.count(queryWrapper);

            if(groupData.getMaxMemberCount() <= count) {
                storeLog(req.getAppId(),req.getOperator(),0,"审批群申请");
                throw new ApplicationException(GroupErrorCode.GROUP_MEMBER_IS_BEYOND);
            }

            ImGroupRequestEntity update = new ImGroupRequestEntity();
            QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
            query.eq("app_id",req.getAppId());
            query.eq("group_id",imGroupRequestEntity.getGroupId());
            query.ne("approve_status",1);

            update.setApproveStatus(req.getStatus());
            update.setSequence(seq);
            update.setId(req.getId());
            imGroupRequestMapper.updateById(update);

            //同意 ===> 去执行添加好友逻辑
            GroupMemberDto dto = new GroupMemberDto();
            dto.setJoinType(imGroupRequestEntity.getAddSource());
            dto.setJoinTime(new Date());
            dto.setMemberId(imGroupRequestEntity.getMemberId());

            ResponseVO responseVO = imGroupMemberService.addGroupMember(imGroupRequestEntity.getGroupId(),req.getAppId(),dto);
            if(!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()){
                storeLog(req.getAppId(),req.getOperator(),0,"审批群申请");
                return responseVO;
            }
        }else{
            ImGroupRequestEntity update = new ImGroupRequestEntity();
            QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
            query.eq("app_id",req.getAppId());
            query.eq("group_id",imGroupRequestEntity.getGroupId());

            update.setApproveStatus(req.getStatus());
            update.setSequence(seq);
            update.setId(req.getId());
            imGroupRequestMapper.updateById(update);
        }

        ApproveGroupRequestPack approveGroupRequestPack = new ApproveGroupRequestPack();
        approveGroupRequestPack.setId(req.getId());
        approveGroupRequestPack.setSequence(seq);
        approveGroupRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(imGroupRequestEntity.getMemberId(),req.getClientType(),req.getImei(), GroupEventCommand.ADDED_MEMBER,approveGroupRequestPack,req.getAppId());
        storeLog(req.getAppId(),req.getOperator(),1,"审批群申请");
        return ResponseVO.successResponse("申请已审批");
    }

    @Override
    public ResponseVO getGroupRequest(GetGroupRequestReq req) {

        LambdaQueryWrapper<ImGroupRequestEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImGroupRequestEntity::getAppId, req.getAppId());
        queryWrapper.eq(ImGroupRequestEntity::getDelFlag, DelFlagEnum.NORMAL);
        queryWrapper.and(wrapper->wrapper.eq(ImGroupRequestEntity::getMemberId,req.getFromId()).or().eq(ImGroupRequestEntity::getOperatorId,req.getFromId()));
        queryWrapper.orderByDesc(ImGroupRequestEntity::getUpdateTime);
        List<ImGroupRequestEntity> requests = imGroupRequestMapper.selectList(queryWrapper);

        requests.forEach(e->{
            GetGroupReq getGroupReq = new GetGroupReq();
            getGroupReq.setAppId(req.getAppId());
            getGroupReq.setGroupId(e.getGroupId());
            getGroupReq.setOperator(req.getOperator());
            ResponseVO<ImGroupEntity> group = imGroupService.getGroup(getGroupReq);
            if(e.getOperatorId() == e.getMemberId()){
                GetUserInfoReq getUserInfoReq = new GetUserInfoReq();
                getUserInfoReq.setUserId(e.getMemberId());
                getGroupReq.setAppId(req.getAppId());
                ResponseVO userInfo = imUserService.getUserInfo(getUserInfoReq);
                if (ObjectUtils.isNotEmpty(userInfo.getData())){
                    e.getParam().put("memberInfo",userInfo.getData());
                }
            }else{
                ResponseVO userInfo = imUserService.getSingleUserInfo(e.getMemberId(),e.getAppId());
                if (ObjectUtils.isNotEmpty(userInfo.getData())){
                    e.getParam().put("memberInfo",userInfo.getData());
                }
                ResponseVO inviter = imUserService.getSingleUserInfo(e.getOperatorId(),e.getAppId());
                if (ObjectUtils.isNotEmpty(inviter.getData())){
                    e.getParam().put("inviterInfo",inviter.getData());
                }
            }
            e.getParam().put("groupInfo",group.getData());
        });


        GetJoinedGroupReq getJoinedGroupReq = new GetJoinedGroupReq();
        getJoinedGroupReq.setMemberId(req.getFromId());
        getJoinedGroupReq.setAppId(req.getAppId());
        ResponseVO<Collection<String>> memberJoinedGroup = imGroupMemberService.getMemberJoinedGroup(getJoinedGroupReq);
        if (CollectionUtils.isEmpty(memberJoinedGroup.getData()) || !memberJoinedGroup.isOk()){
            return ResponseVO.successResponse(requests);
        }
        Collection<String> data = memberJoinedGroup.getData();
        List<String> groups = new ArrayList<>();

        data.forEach(e->{
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(e, req.getFromId(), req.getAppId());
            boolean isAdmin = roleInGroupOne.getData().getRole() == GroupMemberRoleEnum.OWNER.getCode() || roleInGroupOne.getData().getRole() == GroupMemberRoleEnum.MAMAGER.getCode() ? true : false;
            if(roleInGroupOne.isOk() && isAdmin){
                groups.add(e);
            }
        });
        if(CollectionUtils.isNotEmpty(groups)){
            LambdaQueryWrapper<ImGroupRequestEntity> query = new LambdaQueryWrapper();
            query.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImGroupRequestEntity::getAppId, req.getAppId());
            query.eq(ImGroupRequestEntity::getDelFlag, DelFlagEnum.NORMAL);
            query.in(ImGroupRequestEntity::getGroupId,groups);
            query.orderByDesc(ImGroupRequestEntity::getUpdateTime);
            List<ImGroupRequestEntity> list = imGroupRequestMapper.selectList(query);

            list.forEach(e->{
                GetGroupReq getGroupReq = new GetGroupReq();
                getGroupReq.setAppId(req.getAppId());
                getGroupReq.setGroupId(e.getGroupId());
                getGroupReq.setOperator(req.getOperator());
                ResponseVO<ImGroupEntity> group = imGroupService.getGroup(getGroupReq);
                if(e.getOperatorId() == e.getMemberId()){
                    GetUserInfoReq getUserInfoReq = new GetUserInfoReq();
                    getUserInfoReq.setUserId(e.getMemberId());
                    getGroupReq.setAppId(req.getAppId());
                    ResponseVO userInfo = imUserService.getUserInfo(getUserInfoReq);
                    if (ObjectUtils.isNotEmpty(userInfo.getData())){
                        e.getParam().put("memberInfo",userInfo.getData());
                    }
                }else{
                    ResponseVO userInfo = imUserService.getSingleUserInfo(e.getMemberId(),e.getAppId());
                    if (ObjectUtils.isNotEmpty(userInfo.getData())){
                        e.getParam().put("memberInfo",userInfo.getData());
                    }
                    ResponseVO inviter = imUserService.getSingleUserInfo(e.getOperatorId(),e.getAppId());
                    if (ObjectUtils.isNotEmpty(inviter.getData())){
                        e.getParam().put("inviterInfo",inviter.getData());
                    }
                }
                e.getParam().put("groupInfo",group.getData());
            });
            if (CollectionUtils.isNotEmpty(requests)){
                requests.forEach(e->{
                    list.add(e);
                });
            }
            return ResponseVO.successResponse(list);
        }

        return ResponseVO.successResponse(requests);

    }

//    @Override
//    public ResponseVO readGroupRequestReq(ReadGroupRequestReq req) {
//        QueryWrapper<ImGroupRequestEntity> query = new QueryWrapper<>();
//        query.eq("app_id", req.getAppId());
//        query.eq("group_id", req.get());
//
//        long seq = redisSeq.doGetSeq(req.getAppId()+":"+ Constants.SeqConstants.GroupRequest);
//        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
//        update.setReadStatus(1);
//        update.setSequence(seq);
//        imGroupRequestMapper.update(update, query);
//        writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(), Constants.SeqConstants.GroupRequest,seq);
////        TCP通知
//        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
//        readAllFriendRequestPack.setFromId(req.getFromId());
//        readAllFriendRequestPack.setSequence(seq);
//        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImei(), GroupEventCommand.GROUP_REQUEST_READ,readAllFriendRequestPack,req.getAppId());
//
//        return ResponseVO.successResponse();
//    }

    private void storeLog(Integer appId, String userId ,int status,String text) {
        SysLog sysLog = new SysLog();
        Object obj = redisTemplate.opsForHash().get(appId + Constants.RedisConstants.UserSessionConstants + userId, "1:windows");
        JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
        sysLog.setUserId(object.getString("userId"));
        sysLog.setAppId(Integer.valueOf(object.getString("appId")));
        sysLog.setBrokerHost(object.getString("brokerHost"));
        sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
        sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));
        sysLog.setClientType(Integer.valueOf(object.getString("clientType")));
        sysLog.setStatus(status);
        sysLog.setImei(object.getString("imei"));
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
    }
}
