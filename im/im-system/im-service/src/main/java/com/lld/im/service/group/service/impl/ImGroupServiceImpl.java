package com.lld.im.service.group.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.group.CreateGroupPack;
import com.lld.im.codec.pack.group.DestroyGroupPack;
import com.lld.im.codec.pack.group.UpdateGroupInfoPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.*;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.exception.ApplicationException;

import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.common.model.SysLog;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMapper;
import com.lld.im.service.group.model.callback.DestroyGroupCallbackDto;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.*;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.message.dao.ImMessageFileEntity;
import com.lld.im.service.message.dao.mapper.ImMessageFileMapper;
import com.lld.im.service.role.dao.ImRoleEntity;
import com.lld.im.service.role.dao.mapper.ImRoleMapper;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.GroupMessageProducer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class ImGroupServiceImpl extends ServiceImpl<ImGroupMapper,ImGroupEntity> implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupDataMapper;

    @Autowired
    ImGroupMemberService groupMemberService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private ImFriendShipMapper imFriendShipMapper;

    @Autowired
    private ImUserDataMapper imUserDataMapper;

    @Value("${group.logoUploadPath}")
    private String logoUploadPath;

    @Value("${group.logoAdminPath}")
    private String logoAdminPath;

    @Value("${group.logoPath}")
    private String logoPath;


    @Autowired
    ImRoleMapper imRoleMapper;


    @Autowired
    ImMessageFileMapper imMessageFileMapper;

    @Override
    public ResponseVO importGroup(ImportGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupDataMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();

        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        int insert = imGroupDataMapper.insert(imGroupEntity);

        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse("群聊创建成功！");
    }

    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperator());
        }

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(String.valueOf(RandomUtil.randomLong(10000000000L,100000000000L)));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupDataMapper.selectCount(query);
            if (integer > 0) {
                storeLog(req.getAppId(),req.getOperator(),0,"创建群聊");
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            storeLog(req.getAppId(),req.getOperator(),0,"创建群聊");
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        imGroupEntity.setPhoto("\\src\\assets\\img\\profile.jpg");
        imGroupDataMapper.insert(imGroupEntity);

        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.setMemberId(req.getOwnerId());
        groupMemberDto.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupMemberDto.setJoinType(GroupJoinTypeEnum.OWNER_CREATE.getValue());
        groupMemberDto.setJoinTime(new Date());
        groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupMemberDto);

        if (CollectionUtils.isNotEmpty(req.getAdmins())){
            //插入管理员
            for (String memberId : req.getAdmins()) {
                GroupMemberDto dto = new GroupMemberDto();
                dto.setMemberId(memberId);
                dto.setMute(req.getMute());
                dto.setRole(GroupMemberRoleEnum.MAMAGER.getCode());
                dto.setJoinType(GroupJoinTypeEnum.MEMBER_INVITE.getValue());
                dto.setJoinTime(new Date());
                groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
            }
        }

       if (CollectionUtils.isNotEmpty(req.getMembers())){
           //插入群成员
           for (String memberId : req.getMembers()) {
               GroupMemberDto dto = new GroupMemberDto();
               dto.setMemberId(memberId);
               dto.setMute(req.getMute());
               dto.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
               dto.setJoinType(GroupJoinTypeEnum.MEMBER_INVITE.getValue());
               dto.setJoinTime(new Date());
               groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
           }
       }

        if(appConfig.isCreateGroupAfterCallback()){
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.CreateGroupAfter, JSONObject.toJSONString(imGroupEntity));
        }

        CreateGroupPack createGroupPack = new CreateGroupPack();
        BeanUtils.copyProperties(imGroupEntity, createGroupPack);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.CREATED_GROUP, createGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        storeLog(req.getAppId(),req.getOperator(),1,"创建群聊");
        return ResponseVO.successResponse("群聊创建成功！");
    }

    /**
     * @param req
     * @return com.lld.im.common.ResponseVO
     * @description 修改群基础信息，如果是后台管理员调用，则不检查权限，如果不是则检查权限，如果是私有群（微信群）任何人都可以修改资料，公开群只有管理员可以修改
     * 如果是群主或者管理员可以修改其他信息。
     * @author chackylee
     */
    @Override
    @Transactional
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("group_id", req.getGroupId());
        query.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);
        if (ObjectUtils.isEmpty(imGroupEntity)) {
            storeLog(req.getAppId(),req.getOperator(),0,"修改群信息");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
            storeLog(req.getAppId(),req.getOperator(),0,"修改群信息");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());

            if (!role.isOk()) {
                storeLog(req.getAppId(),req.getOperator(),0,"修改群信息");
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //公开群只能群主修改资料
            if (!isManager && GroupTypeEnum.PUBLIC.getCode() == imGroupEntity.getGroupType()) {
                storeLog(req.getAppId(),req.getOperator(),0,"修改群信息");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

        }

        ImGroupEntity update = new ImGroupEntity();
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        BeanUtils.copyProperties(req, update);
        update.setSequence(seq);
        int row = imGroupDataMapper.update(update, query);
        if (row != 1) {
            storeLog(req.getAppId(),req.getOperator(),0,"修改群信息");
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

            GetGroupReq getGroupReq = new GetGroupReq();
            getGroupReq.setGroupId(req.getGroupId());
            getGroupReq.setOperator(req.getOperator());
            getGroupReq.setAppId(req.getAppId());
            ResponseVO group = getGroup(getGroupReq);
            if(appConfig.isModifyGroupAfterCallback()){
                callbackService.callback(req.getAppId(),Constants.CallbackCommand.UpdateGroupAfter, JSONObject.toJSONString(imGroupDataMapper.selectOne(query)));
            }

            UpdateGroupInfoPack pack = new UpdateGroupInfoPack();
            BeanUtils.copyProperties(req, pack);
            groupMessageProducer.producer(req.getOperator(), GroupEventCommand.UPDATED_GROUP, pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

            storeLog(req.getAppId(),req.getOperator(),1,"修改群信息");
            return ResponseVO.successResponse(group.getData());
    }

    // 获取用户加入的群组
    @Override
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req) {

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.getMemberJoinedGroup(req);
        if (memberJoinedGroup.isOk()) {

            GetJoinedGroupResp resp = new GetJoinedGroupResp();

            if (CollectionUtils.isEmpty(memberJoinedGroup.getData())) {
                resp.setTotalCount(0);
                resp.setGroupList(new ArrayList<>());
                return ResponseVO.successResponse(resp);
            }

            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("status", 1);
            query.in("group_id", memberJoinedGroup.getData());

            if (CollectionUtils.isNotEmpty(req.getGroupType())) {
                query.in("group_type", req.getGroupType());
            }

            List<ImGroupEntity> groupList = imGroupDataMapper.selectList(query);
            groupList.forEach(e->{
                LambdaQueryWrapper<ImGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImGroupMemberEntity::getGroupId,e.getGroupId());
                wrapper.eq(ImGroupMemberEntity::getAppId,e.getAppId());

                List<ImGroupMemberEntity> groupMembers = groupMemberService.list(wrapper);
                e.getParam().put("memberCount",groupMembers.size());
                e.getParam().put("groupMembers",groupMembers);
            });
            resp.setGroupList(groupList);
            if (req.getLimit() == null) {
                resp.setTotalCount(groupList.size());
            } else {
                resp.setTotalCount(imGroupDataMapper.selectCount(query));
            }
            return ResponseVO.successResponse(resp);
        } else {
            return memberJoinedGroup;
        }
    }


    //解散群组，只支持后台管理员和群主解散
    @Override
    @Transactional
    public ResponseVO destroyGroup(DestroyGroupReq req) {

        boolean isAdmin = false;

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(objectQueryWrapper);
        if (imGroupEntity == null) {
            storeLog(req.getAppId(),req.getOperator(),0,"解散群聊");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
            storeLog(req.getAppId(),req.getOperator(),0,"解散群聊");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

//        if (!isAdmin) {
//            //私有群只能管理员解散
//            if (imGroupEntity.getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
//                storeLog(req.getAppId(),req.getOperator(),0,"解散群聊");
//                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
//            imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
//            }

        //公开群可以群主解散
        if (!imGroupEntity.getOwnerId().equals(req.getOperator())) {
            storeLog(req.getAppId(),req.getOperator(),0,"解散群聊");
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }
//        }

        ImGroupEntity update = new ImGroupEntity();
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);

        update.setStatus(GroupStatusEnum.DESTROY.getCode());
        update.setSequence(seq);
        int count = imGroupDataMapper.update(update, objectQueryWrapper);
        if (count != 1) {
            storeLog(req.getAppId(),req.getOperator(),0,"解散群聊");
            throw new ApplicationException(GroupErrorCode.DELETE_GROUP_BASE_INFO_ERROR);
        }

        if(appConfig.isModifyGroupAfterCallback()){
            DestroyGroupCallbackDto dto = new DestroyGroupCallbackDto();
            dto.setGroupId(req.getGroupId());
            callbackService.callback(req.getAppId()
                    ,Constants.CallbackCommand.DestoryGroupAfter,
                    JSONObject.toJSONString(dto));
        }

        DestroyGroupPack pack = new DestroyGroupPack();
        pack.setSequence(seq);
        pack.setGroupId(req.getGroupId());
        groupMessageProducer.producer(req.getOperator(),GroupEventCommand.DESTROY_GROUP, pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        storeLog(req.getAppId(),req.getOperator(),1,"解散群聊");
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!roleInGroupOne.isOk()) {
            storeLog(req.getAppId(),req.getOperator(),0,"群主转让");
            return roleInGroupOne;
        }

        if (roleInGroupOne.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            storeLog(req.getAppId(),req.getOperator(),0,"群主转让");
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        ResponseVO<GetRoleInGroupResp> newOwnerRole = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOwnerId(), req.getAppId());
        if (!newOwnerRole.isOk()) {
            storeLog(req.getAppId(),req.getOperator(),0,"群主转让");
            return newOwnerRole;
        }

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(objectQueryWrapper);
        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
            storeLog(req.getAppId(),req.getOperator(),0,"群主转让");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        ImGroupEntity updateGroup = new ImGroupEntity();
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        updateGroup.setSequence(seq);
        updateGroup.setOwnerId(req.getOwnerId());
        UpdateWrapper<ImGroupEntity> updateGroupWrapper = new UpdateWrapper<>();
        updateGroupWrapper.eq("app_id", req.getAppId());
        updateGroupWrapper.eq("group_id", req.getGroupId());
        imGroupDataMapper.update(updateGroup, updateGroupWrapper);
        groupMemberService.transferGroupMember(req.getOwnerId(), req.getGroupId(), req.getAppId());

        storeLog(req.getAppId(),req.getOperator(),1,"群主转让");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String groupId, Integer appId) {

        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);

        if (imGroupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(imGroupEntity);
    }

    @Override
    public ResponseVO getGroup(GetGroupReq req) {

        ResponseVO group = this.getGroup(req.getGroupId(), req.getAppId());

        if (!group.isOk()) {
            return group;
        }
        GetGroupResp getGroupResp = new GetGroupResp();
        BeanUtils.copyProperties(group.getData(), getGroupResp);
        ImGroupMemberEntity groupMemberByMemberId = groupMemberService.getGroupMemberByMemberId(req.getGroupId(), req.getAppId(), req.getOperator());
        if (ObjectUtils.isNotEmpty(groupMemberByMemberId)) {
            getGroupResp.setIsMember(1);
        }
        try {
            ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
            if (groupMember.isOk()) {
                List<GroupMemberDto> data = groupMember.getData();
                data.forEach(e->{
                    LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
                    query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImFriendShipEntity::getAppId, req.getAppId());
                    query.eq(StringUtils.isNotEmpty(req.getOperator()), ImFriendShipEntity::getFromId, req.getOperator());
                    query.eq(StringUtils.isNotEmpty(e.getMemberId()), ImFriendShipEntity::getToId, e.getMemberId());
                    query.eq(ImFriendShipEntity::getStatus, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                    query.eq(ImFriendShipEntity::getBlack, FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                    ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
                    if(ObjectUtils.isNotEmpty(entity)){
                        e.setIsFriend(1);
                    }

                    boolean isManager = GroupMemberRoleEnum.OWNER.getCode()== e.getRole() || GroupMemberRoleEnum.MAMAGER.getCode()== e.getRole() ? true :false;
                    LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImUserDataEntity::getAppId, req.getAppId());
                    wrapper.eq(StringUtils.isNotEmpty(e.getMemberId()), ImUserDataEntity::getUserId, e.getMemberId());
                    wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

                    ImUserDataEntity userData = imUserDataMapper.selectOne(wrapper);


                    if (e.getMemberId().equals(req.getOperator()) && StringUtils.isNotEmpty(e.getAlias())){
                        getGroupResp.setGroupNickName(e.getAlias());
                    }

                    if (e.getMemberId().equals(req.getOperator())){
                        getGroupResp.setMemberMute(e.getMute());
                        e.setIsFriend(2);
                    }

                    if(StringUtils.isEmpty(e.getAlias())){
                        e.setAlias(userData.getNickName());
                        getGroupResp.setNickName(userData.getNickName());
                    }
                    if (e.getMemberId().equals(req.getOperator()) && isManager){
                        getGroupResp.setIsManager(1);
                    }
                    e.setPhoto(userData.getPhoto());
                });
                getGroupResp.setMemberList(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseVO.successResponse(getGroupResp);
    }

    @Override
    public ResponseVO muteGroup(MuteGroupReq req) {

        ResponseVO<ImGroupEntity> groupResp = getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            storeLog(req.getAppId(),req.getOperator(),0,"禁言群聊");
            return groupResp;
        }

        if(groupResp.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()){
            storeLog(req.getAppId(),req.getOperator(),0,"禁言群聊");
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperator(), req.getAppId());

            if (!role.isOk()) {
                storeLog(req.getAppId(),req.getOperator(),0,"禁言群聊");
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //公开群只能群主修改资料
            if (!isManager) {
                storeLog(req.getAppId(),req.getOperator(),0,"禁言群聊");
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();
        update.setMute(req.getMute());

        UpdateWrapper<ImGroupEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id",req.getGroupId());
        wrapper.eq("app_id",req.getAppId());
        imGroupDataMapper.update(update,wrapper);

        storeLog(req.getAppId(),req.getOperator(),1,"禁言群聊");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO syncJoinedGroupList(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImGroupEntity> resp = new SyncResp<>();

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.syncMemberJoinedGroup(req.getOperator(), req.getAppId());
        if(memberJoinedGroup.isOk()){

            Collection<String> data = memberJoinedGroup.getData();
            QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id",req.getAppId());
            queryWrapper.in("group_id",data);
            queryWrapper.gt("sequence",req.getLastSequence());
            queryWrapper.last(" limit " + req.getMaxLimit());
            queryWrapper.orderByAsc("sequence");

            List<ImGroupEntity> list = imGroupDataMapper.selectList(queryWrapper);

            if(!CollectionUtils.isEmpty(list)){
                ImGroupEntity maxSeqEntity = list.get(list.size() - 1);
                resp.setDataList(list);
                //设置最大seq
                Long maxSeq = imGroupDataMapper.getGroupMaxSeq(data, req.getAppId());
                resp.setMaxSequence(maxSeq);
                //设置是否拉取完毕
                resp.setCompleted(maxSeqEntity.getSequence() >= maxSeq);
                return ResponseVO.successResponse(resp);
            }

        }
        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public Long getUserGroupMaxSeq(String userId, Integer appId) {

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.syncMemberJoinedGroup(userId, appId);
        if(!memberJoinedGroup.isOk()){
            throw new ApplicationException(500,"");
        }
        Long maxSeq = imGroupDataMapper.getGroupMaxSeq(memberJoinedGroup.getData(), appId);
        return maxSeq;
    }

    @Override
    public ResponseVO searchGroup(SearchGroupReq req) {
        List<SearchGroupResp> list = new ArrayList<>();
        LambdaQueryWrapper<ImGroupEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImGroupEntity::getAppId,req.getAppId());
        query.eq(ImGroupEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
        query.and(wrapper->wrapper.like(StringUtils.isNotEmpty(req.getSearchInfo()),ImGroupEntity::getGroupId,req.getSearchInfo())
                                  .or().like(StringUtils.isNotEmpty(req.getSearchInfo()),ImGroupEntity::getGroupName,req.getSearchInfo()));

        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
        imGroupEntities.forEach(e->{
            SearchGroupResp searchGroupResp = new SearchGroupResp();
            BeanUtils.copyProperties(e,searchGroupResp);
            searchGroupResp.setType(2);

            ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(e.getGroupId(), e.getAppId());
            ImGroupMemberEntity groupMemberByMemberId = groupMemberService.getGroupMemberByMemberId(e.getGroupId(), e.getAppId(), req.getOperator());
            if(ObjectUtils.isEmpty(groupMemberByMemberId)){
                searchGroupResp.setIsMember(0);
            }else {
                searchGroupResp.setIsMember(1);

            }

            searchGroupResp.setGroupMembers(groupMember.getData());
            list.add(searchGroupResp);
        });
        return ResponseVO.successResponse(list);

    }

    @Override
    @Transactional
    public ResponseVO uploadLogo(HttpServletRequest request,MultipartFile uploadFile,String groupId,String operator) throws IOException {
        ImUserDataEntity userData = imUserDataMapper.selectById(operator);
        if(ObjectUtils.isEmpty(userData)){
            return ResponseVO.errorResponse(500,"操作用户不存在");
        }

        ImGroupEntity groupEntity = imGroupDataMapper.selectById(groupId);
        if(ObjectUtils.isEmpty(groupEntity)){
            return ResponseVO.errorResponse(500,"组群不存在");
        }

        ImRoleEntity imRoleEntity = imRoleMapper.selectById(userData.getRoleId());
        if(ObjectUtils.isEmpty(imRoleEntity)){
            return ResponseVO.errorResponse(500,"操作人为非角色用户");
        }

        Integer isAdmin = RoleTypeEnum.ADMIN.getValue().equals(imRoleEntity.getName()) || RoleTypeEnum.SUPER_ADMIN.getValue().equals(imRoleEntity.getName()) ? 1 : 0;


        if ( uploadFile != null) {
            //获得上传文件的文件名
            String oldName = uploadFile.getOriginalFilename();
            String suffix = oldName.substring(oldName.indexOf('.'));
            System.out.println("[上传的文件名]：" + oldName);
            System.out.println("[上传的文件]：" + uploadFile.getBytes().length);
            //我的文件保存在static目录下的avatar/user
            String name =  groupId + suffix;
            String fileName =  logoUploadPath + groupId + suffix;
            String adminFileName = logoAdminPath + groupId + suffix;
            System.out.println("[fileName]：" + fileName);
            System.out.println("[adminFileName]：" + adminFileName);

            File photo = new File(fileName);
            File adminPhoto = new File(adminFileName);
            try {
                if (!photo.exists()) {
                    photo.createNewFile();
                }
                if (!adminPhoto.exists()) {
                    adminPhoto.createNewFile();
                }
                //创建BufferedReader读取文件内容
                uploadFile.getInputStream();
                FileOutputStream photoInput = new FileOutputStream(photo);
                photoInput.write(uploadFile.getBytes());
                photoInput.close();

                FileOutputStream adminInput = new FileOutputStream(adminPhoto);
                adminInput.write(uploadFile.getBytes());
                adminInput.close();

                if (isAdmin == 1) {
                    storeLog(request, groupEntity.getAppId(), operator, 1, "群头像修改");
                } else {
                    storeLog(groupEntity.getAppId(), operator, 1, "群头像修改");
                }


                //返回成功结果，附带文件的相对路径
                String picture = logoPath + name;

                LambdaQueryWrapper<ImMessageFileEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImMessageFileEntity::getName,name);
                wrapper.eq(ImMessageFileEntity::getLocation,picture);
                wrapper.eq(ImMessageFileEntity::getType,2);
                ImMessageFileEntity imMessageFileEntity = imMessageFileMapper.selectOne(wrapper);
                if(ObjectUtils.isEmpty(imMessageFileEntity)){
                    ImMessageFileEntity fileEntity = new ImMessageFileEntity();
                    fileEntity.setType(2);
                    fileEntity.setName(name);
                    fileEntity.setLocation(picture);
                    fileEntity.setSize(uploadFile.getSize());
                    fileEntity.setOperatorId(operator);
                    fileEntity.setExtension(suffix);
                    imMessageFileMapper.insert(fileEntity);
                }
                if (!groupEntity.getPhoto().equals(picture)){
                    groupEntity.setPhoto(picture);
                    imGroupDataMapper.updateById(groupEntity);
                }
                return ResponseVO.successResponse(picture);
            } catch (IOException e) {
                e.printStackTrace();
                if (isAdmin == 1) {
                    storeLog(request, groupEntity.getAppId(), operator, 0, "群头像修改");
                } else {
                    storeLog(groupEntity.getAppId(), operator, 0, "群头像修改");
                }
                return ResponseVO.errorResponse(500, "群头像修改");
            }

        }
        if (isAdmin == 1){
            storeLog(groupEntity.getAppId(), operator,0,"群头像修改");
        }else {
            storeLog(groupEntity.getAppId(), operator,0,"群头像修改");
        }
        System.out.println("上传的文件为空");
        return ResponseVO.errorResponse(500, "上传的文件为空");
    }

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

    private void storeLog(HttpServletRequest request,Integer appId, String userId ,int status,String text) {
        SysLog sysLog = new SysLog();
        sysLog.setUserId(userId);
        sysLog.setAppId(appId);
        sysLog.setBrokerHost(request.getRemoteHost());
        sysLog.setConnectState(1);
        sysLog.setClientType(1);
        sysLog.setImei("windows");
        sysLog.setStatus(status);
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants, JSONObject.toJSONString(sysLog),new Date().getTime());

    }

}
