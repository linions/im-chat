package com.lld.im.service.user.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.user.UserModifyPack;
import com.lld.im.common.ClientType;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.RoleTypeEnum;
import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.SysLog;
import com.lld.im.common.model.message.FileDto;
import com.lld.im.common.utils.HttpRequestUtils;
import com.lld.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.message.dao.ImMessageFileEntity;
import com.lld.im.service.message.dao.mapper.ImMessageFileMapper;
import com.lld.im.service.role.dao.ImRoleEntity;
import com.lld.im.service.role.dao.mapper.ImRoleMapper;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.user.model.UserStatusChangeNotifyContent;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.model.resp.*;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.user.service.ImUserStatusService;
import com.lld.im.service.utils.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.lld.im.common.utils.LinearRegression.initAndGet;

@Service
public class ImUserServiceImpl extends ServiceImpl<ImUserDataMapper, ImUserDataEntity> implements ImUserService{

    @Autowired
    ImUserDataMapper imUserDataMapper;

    @Autowired
    ImMessageFileMapper imMessageFileMapper;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Autowired
    ImRoleMapper imRoleMapper;


    @Autowired
    RedisTemplate redisTemplate;

    @Value("${user.logoUploadPath}")
    private String logoUploadPath;

    @Value("${user.logoAdminPath}")
    private String logoAdminPath;

    @Value("${user.logoPath}")
    private String logoPath;

    @Autowired
    ImUserStatusService imUserStatusService;


    @Override
    public ResponseVO importUser(ImportUserReq req) {

        if(req.getUserData().size() > 100){
            return ResponseVO.errorResponse(UserErrorCode.IMPORT_SIZE_BEYOND);
        }

        ImportUserResp resp = new ImportUserResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        List<ImUserDataEntity> userData = req.getUserData();
        if(!CollectionUtils.isEmpty(userData)){
            for (ImUserDataEntity data : userData) {
                try {
                    data.setAppId(req.getAppId());
                    int insert = imUserDataMapper.insert(data);
                    if(insert == 1){
                        successId.add(data.getUserId());
                    }else {
                        errorId.add(data.getUserId());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    errorId.add(data.getUserId());
                }
            }

            resp.setErrorId(errorId);
            resp.setSuccessId(successId);
            return ResponseVO.successResponse(resp);
        }
        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO getUserInfo(GetUserInfoReq req) {

        LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
        queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
        queryWrapper.eq(ImUserDataEntity::getRoleId, 3);
        queryWrapper.and(query ->query.like(StringUtils.isNotEmpty(req.getUserId()),ImUserDataEntity::getUserId,req.getUserId())
                                    .or().like(StringUtils.isNotEmpty(req.getNickName()),ImUserDataEntity::getNickName,req.getNickName())
                                    .or().like(StringUtils.isNotEmpty(req.getEmail()),ImUserDataEntity::getMobile,req.getEmail())
                );

        List<ImUserDataEntity> userDataEntities = imUserDataMapper.selectList(queryWrapper);
        return ResponseVO.successResponse(userDataEntities);
    }

    @Override
    public ResponseVO<GetSingleUserInfoResp> getSingleUserInfo(String userId, Integer appId) {
        GetSingleUserInfoResp userInfoResp = new GetSingleUserInfoResp();
        LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId),ImUserDataEntity::getAppId,appId);
        queryWrapper.eq(StringUtils.isNotEmpty(userId),ImUserDataEntity::getUserId,userId);
        queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity imUserData = imUserDataMapper.selectOne(queryWrapper);
        if(imUserData == null){
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }

        BeanUtils.copyProperties(imUserData,userInfoResp);
        userInfoResp.setIsAdmin(0);

        LambdaQueryWrapper<ImRoleEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImRoleEntity::getRoleId,imUserData.getRoleId());
        query.eq(ImRoleEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImRoleEntity imRole = imRoleMapper.selectOne(query);
        if(ObjectUtils.isNotEmpty(imRole)){
            if(RoleTypeEnum.SUPER_ADMIN.getValue().equals(imRole.getName())){
                userInfoResp.setIsAdmin(2);
            } else if (RoleTypeEnum.ADMIN.getValue().equals(imRole.getName())) {
                userInfoResp.setIsAdmin(1);
            }
            userInfoResp.setRole(imRole);
        }

        return ResponseVO.successResponse(userInfoResp);
    }


    @Override
    public ResponseVO<ImUserDataEntity> login(LoginReq req) {
        //登录
        if(req.getLoginType() == 1){
//            账号密码登录
           if (req.getType() == 1){
               LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
               wrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
               wrapper.eq(ImUserDataEntity::getUserId,req.getUserId());
               wrapper.eq(ImUserDataEntity::getPassword,req.getPassword());
               wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

               ImUserDataEntity userData = imUserDataMapper.selectOne(wrapper);
               if(ObjectUtils.isNotEmpty(userData)) {
                   if(userData.getSilentFlag() == 1){
                       return ResponseVO.errorResponse(500,"用户已被禁用，请联系管理员处理");
                   }
                   userData.setStatus(1);
                   imUserDataMapper.update(userData,wrapper);
                   sendNotify(userData.getAppId(),userData.getUserId(),1);
                   return ResponseVO.successResponse(userData);
               }
           }else if (req.getType() == 2) {
//               手机验证码登录
               String mobile = req.getMobile();
               String code = req.getCode();
               String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + mobile;
               String redisCode = (String) redisTemplate.opsForValue().get(key);
               if (redisCode != null && redisCode.equals(code)) {
                   LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
                   wrapper.eq(ImUserDataEntity::getMobile,req.getMobile());
                   wrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
                   wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

                   ImUserDataEntity userData = imUserDataMapper.selectOne(wrapper);
                   if(ObjectUtils.isNotEmpty(userData)) {
                       if(userData.getSilentFlag() == 1){
                           return ResponseVO.errorResponse(500,"用户已被禁用，请联系管理员处理");
                       }
                       userData.setStatus(1);
                       imUserDataMapper.update(userData,wrapper);
                       sendNotify(userData.getAppId(),userData.getUserId(),1);
                       return ResponseVO.successResponse(userData);
                   }
               }
           }else if (req.getType() == 3){
//               邮箱验证码登录
               String email = req.getEmail();
               String code = req.getCode();
               String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + email;
               String redisCode = (String) redisTemplate.opsForValue().get(key);
               if(redisCode != null && redisCode.equals(code)){
                   LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
                   wrapper.eq(ImUserDataEntity::getEmail,req.getEmail());
                   wrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
                   wrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

                   ImUserDataEntity userData = imUserDataMapper.selectOne(wrapper);
                   if(ObjectUtils.isNotEmpty(userData)) {
                       if(userData.getSilentFlag() == 1){
                           return ResponseVO.errorResponse(500,"用户已被禁用，请联系管理员处理");
                       }
                       userData.setStatus(1);
                       imUserDataMapper.update(userData,wrapper);
                       sendNotify(userData.getAppId(),userData.getUserId(),1);
                       return ResponseVO.successResponse(userData);
                   }
               }

                return ResponseVO.errorResponse(500,"登录失败");
           }
        }else {
            //注册
            LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImUserDataEntity::getAppId,req.getAppId());
            Integer integer = imUserDataMapper.selectCount(wrapper) + 1;
            ImUserDataEntity userData = new ImUserDataEntity();
            StringBuilder snowId = new StringBuilder(SnowflakeIdWorker.nextId());
            StringBuilder reverse = snowId.reverse();//将id翻转：我们发现id很长，且高位很长部分是一样的数
            Long userId = new Long(reverse.toString())/1000;//切去部分长度
            while(userId>1999999999){//1999999999以内的10位或9位或8位id;....
                userId/=10;
            }
            BeanUtils.copyProperties(req,userData);
            userData.setUserId(String.valueOf(userId));
            userData.setNickName("User" + integer);
            userData.setPhoto("\\src\\assets\\img\\profile.jpg");
            userData.setRoleId(3);


           if(req.getType() == 2){
               String mobile = req.getMobile();
               String code = req.getCode();

               String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + mobile;
               String redisCode = (String) redisTemplate.opsForValue().get(key);
               if(redisCode != null && redisCode.equals(code)){
                       userData.setStatus(1);
                       imUserDataMapper.insert(userData);
                       sendNotify(userData.getAppId(),userData.getUserId(),1);
                       return ResponseVO.successResponse(userData);
               }
           } else if (req.getType() == 3) {
               String email = req.getEmail();
               String code = req.getCode();

               String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + email;
               String redisCode = (String) redisTemplate.opsForValue().get(key);
               if(redisCode != null && redisCode.equals(code)){
                       userData.setStatus(1);
                       imUserDataMapper.insert(userData);
                       sendNotify(userData.getAppId(),userData.getUserId(),1);
                       return ResponseVO.successResponse(userData);
               }
           }
        }
        return ResponseVO.errorResponse(500,"登录失败");
    }


    public void sendNotify(Integer appId,String userId,int status){
        UserStatusChangeNotifyContent content = new UserStatusChangeNotifyContent();
        content.setStatus(status);
        content.setAppId(appId);
        content.setUserId(userId);
        imUserStatusService.processUserOnlineStatusNotify(content);
    }
    @Override
    @Transactional
    public ResponseVO modifyUserInfo(HttpServletRequest request,ModifyUserInfoReq req) {
        LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
        queryWrapper.eq(StringUtils.isNotEmpty(req.getUserId()),ImUserDataEntity::getUserId,req.getUserId());
        queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());
        ImUserDataEntity user = imUserDataMapper.selectOne(queryWrapper);
        if(user == null){
            if(req.getIsAdmin() == 1 || req.getIsAdmin() == 2){
                storeLog(request,req.getAppId(),req.getOperator(),0,"修改用户信息");
            }else{
                storeLog(req.getAppId(),req.getUserId(),0,"修改用户信息");
            }
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        ImUserDataEntity userData = new ImUserDataEntity();
        BeanUtils.copyProperties(req,userData);

        if(req.getIsAdmin() == 1 || req.getIsAdmin() == 2){
            userData.setAppId(null);
            userData.setUserId(null);
            imUserDataMapper.update(userData, queryWrapper);
            storeLog(request,req.getAppId(),req.getOperator(),1,"修改用户信息");
            return ResponseVO.successResponse();
        }

//        修改手机
        if(StringUtils.isNotEmpty(req.getMobile())){
            String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + req.getMobile();
            String redisCode = (String) redisTemplate.opsForValue().get(key);
            if (redisCode == null || !redisCode.equals(req.getCode())) {
                    return ResponseVO.errorResponse(500,"验证码有误，修改失败！");
            }
        }

        if(StringUtils.isNotEmpty(req.getEmail())){
            String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + req.getEmail();
            String redisCode = (String) redisTemplate.opsForValue().get(key);
            if (redisCode == null || !redisCode.equals(req.getCode())) {
                return ResponseVO.errorResponse(500,"验证码有误，修改失败！");
            }
        }

        if(StringUtils.isNotEmpty(req.getPassword())){
            if(req.getType() == 2){
                String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + user.getMobile();
                String redisCode = (String) redisTemplate.opsForValue().get(key);
                if (redisCode == null || !redisCode.equals(req.getCode())) {
                    return ResponseVO.errorResponse(500,"验证码有误，修改失败！");
                }
            }
            if(req.getType() == 3){
                String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + user.getEmail();
                String redisCode = (String) redisTemplate.opsForValue().get(key);
                if (redisCode == null || !redisCode.equals(req.getCode())) {
                    return ResponseVO.errorResponse(500,"验证码有误，修改失败！");
                }
            }

        }

        userData.setAppId(null);
        userData.setUserId(null);
        int update = imUserDataMapper.update(userData, queryWrapper);
        if(update == 1){
            //回调
            UserModifyPack pack = new UserModifyPack();
            BeanUtils.copyProperties(req,pack);
            messageProducer.sendToUser(req.getUserId(),req.getClientType(),req.getImei(),UserEventCommand.USER_MODIFY,pack,req.getAppId());

            if(appConfig.isModifyUserAfterCallback()){
                callbackService.callback(req.getAppId(),Constants.CallbackCommand.ModifyUserAfter,JSONObject.toJSONString(req));
            }
            storeLog(req.getAppId(),req.getUserId(),1,"修改用户信息");
            return ResponseVO.successResponse("用户信息修改成功！");
        }
        storeLog(req.getAppId(),req.getUserId(),0,"修改用户信息");
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    @Override
    public ResponseVO getUserSequence(GetUserSequenceReq req) {
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(req.getAppId() + ":" + Constants.RedisConstants.SeqPrefix + ":" + req.getUserId());
        Long groupSeq = imGroupService.getUserGroupMaxSeq(req.getUserId(),req.getAppId());
        map.put(Constants.SeqConstants.Group,groupSeq);
        return ResponseVO.successResponse(map);
    }

    @Override
    public ResponseVO getLoginCode(GetCodeReq req) {
        String verifyCode =  String.valueOf((int)(Math.random()*900000 + 100000));
//        电话验证
        if(req.getType() == 2){
            writeUserSeq.writeCode(req.getAppId(), req.getMobile(), verifyCode);
        }else{
            writeUserSeq.writeCode(req.getAppId(), req.getEmail(),verifyCode);
            MailUtils.sendMail(req.getEmail(),verifyCode);
        }
        return ResponseVO.successResponse("验证码发送成功！");
    }

    @Override
    public ResponseVO findPassword(FindPasswordReq req) {
        FindPasswordResp findPasswordResp = new FindPasswordResp();

        //        修改手机
        if(StringUtils.isNotEmpty(req.getMobile()) && req.getType() == 2){
            String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + req.getMobile();
            String redisCode = (String) redisTemplate.opsForValue().get(key);
            if (redisCode != null && redisCode.equals(req.getCode())) {
            LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
            queryWrapper.eq(StringUtils.isNotEmpty(req.getUserId()),ImUserDataEntity::getUserId,req.getUserId());
            queryWrapper.eq(StringUtils.isNotEmpty(req.getMobile()),ImUserDataEntity::getMobile,req.getMobile());
            queryWrapper.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

            ImUserDataEntity userData = imUserDataMapper.selectOne(queryWrapper);
            BeanUtils.copyProperties(userData,findPasswordResp);
            storeLog(req.getAppId(),req.getUserId(),1,"找回密码");
            return ResponseVO.successResponse(findPasswordResp);
            }
        }

        if(StringUtils.isNotEmpty(req.getEmail()) && req.getType() == 3){
            String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + req.getEmail();
            String redisCode = (String) redisTemplate.opsForValue().get(key);
            if (redisCode != null && redisCode.equals(req.getCode())) {
                LambdaQueryWrapper<ImUserDataEntity> query = new LambdaQueryWrapper<>();
                query.eq(ObjectUtils.isNotEmpty(req.getAppId()), ImUserDataEntity::getAppId, req.getAppId());
                query.eq(StringUtils.isNotEmpty(req.getUserId()), ImUserDataEntity::getUserId, req.getUserId());
                query.eq(StringUtils.isNotEmpty(req.getEmail()), ImUserDataEntity::getEmail, req.getEmail());
                query.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

                ImUserDataEntity userData = imUserDataMapper.selectOne(query);
                BeanUtils.copyProperties(userData, findPasswordResp);
                storeLog(req.getAppId(),req.getUserId(),1,"找回密码");
                return ResponseVO.successResponse(findPasswordResp);
            }
        }
        storeLog(req.getAppId(),req.getUserId(),0,"找回密码");
        return ResponseVO.errorResponse(500,"验证码有误，找回失败！");
    }

    @Override
    public ResponseVO getCertify(GetCertifyReq req) {
        String key = req.getAppId() + ":" + Constants.RedisConstants.userCode + ":" + req.getAccount();
        String redisCode = (String) redisTemplate.opsForValue().get(key);
        if (redisCode != null && redisCode.equals(req.getCode())) {
            return ResponseVO.successResponse("认证成功！");
        }
        return ResponseVO.errorResponse(500,"认证失败！");
    }

    @Override
    public ResponseVO logout(LogoutReq req) {
        LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
        queryWrapper.eq(StringUtils.isNotEmpty(req.getUserId()),ImUserDataEntity::getUserId,req.getUserId());
        queryWrapper.eq(ImUserDataEntity::getStatus, 1);

        ImUserDataEntity userData = imUserDataMapper.selectOne(queryWrapper);
        if(ObjectUtils.isNotEmpty(userData)){
            userData.setStatus(0);
        }
        int i = imUserDataMapper.update(userData,queryWrapper);
        if (i > 0){
            sendNotify(userData.getAppId(),userData.getUserId(),0);
            return ResponseVO.successResponse();
        }
        return ResponseVO.errorResponse(500,req.getUserId());
    }

    @Override
    public ResponseVO getUserByPage(UserReq req) {
//        UserResp userResp = new UserResp();
        if(req.getPage() != 0 && req.getPageSize() != 0){
            IPage<ImUserDataEntity> page = new Page(req.getPage(), req.getPageSize());
            LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
            queryWrapper.like(StringUtils.isNotEmpty(req.getUserId()),ImUserDataEntity::getUserId,req.getUserId());
            queryWrapper.like(StringUtils.isNotEmpty(req.getNickName()),ImUserDataEntity::getNickName,req.getNickName());
            queryWrapper.like(StringUtils.isNotEmpty(req.getMobile()),ImUserDataEntity::getMobile,req.getMobile());
            queryWrapper.like(StringUtils.isNotEmpty(req.getEmail()),ImUserDataEntity::getEmail,req.getEmail());
            queryWrapper.eq(req.getStatus() != 2,ImUserDataEntity::getStatus,req.getStatus());
            queryWrapper.eq(req.getRoleId() != -1,ImUserDataEntity::getRoleId,req.getRoleId());
            queryWrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            queryWrapper.eq(req.getForbiddenFlag() != 2,ImUserDataEntity::getForbiddenFlag,req.getForbiddenFlag());
            if(!CollectionUtils.isEmpty(req.getCreateTime())){
                queryWrapper.between(ImUserDataEntity::getCreateTime,new Timestamp(req.getCreateTime().get(0).getTime()),new Timestamp(req.getCreateTime().get(1).getTime()));
            }
            IPage<ImUserDataEntity> iPage = imUserDataMapper.selectPage(page, queryWrapper);

            List<ImUserDataEntity> records = iPage.getRecords();
            for (ImUserDataEntity record : records) {
                LambdaQueryWrapper<ImRoleEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImRoleEntity::getRoleId,record.getRoleId());
                wrapper.eq(ImRoleEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

                ImRoleEntity imRoleEntity = imRoleMapper.selectOne(wrapper);
                if(ObjectUtils.isNotEmpty(imRoleEntity)){
                    record.getParam().put("role",imRoleEntity);
                }
            }
            return ResponseVO.successResponse(iPage);
        }else {
            LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImUserDataEntity::getAppId,req.getAppId());
            queryWrapper.eq(StringUtils.isNotEmpty(req.getUserId()),ImUserDataEntity::getUserId,req.getUserId());
            queryWrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            List<ImUserDataEntity> imUserDataEntities = imUserDataMapper.selectList(queryWrapper);

            for (ImUserDataEntity record : imUserDataEntities) {
                LambdaQueryWrapper<ImRoleEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImRoleEntity::getRoleId,record.getRoleId());
                wrapper.eq(ImRoleEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

                ImRoleEntity imRoleEntity = imRoleMapper.selectOne(wrapper);
                if(ObjectUtils.isNotEmpty(imRoleEntity)){
                    record.getParam().put("role",imRoleEntity);
                }
            }

            return ResponseVO.successResponse(imUserDataEntities);
        }
    }

    @Override
    public ResponseVO getUserPercentage(Integer appId) {
        List<UserTypeResp> result = new ArrayList<>();

        LambdaQueryWrapper<ImRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImRoleEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

        List<ImRoleEntity> imRoleEntities = imRoleMapper.selectList(wrapper);
        LambdaQueryWrapper<ImUserDataEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImUserDataEntity::getAppId,appId);
        query.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
        Integer userCount = imUserDataMapper.selectCount(query);

        imRoleEntities.forEach(e->{
            LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImUserDataEntity::getAppId,appId);
            queryWrapper.eq(ImUserDataEntity::getRoleId,e.getRoleId());
            queryWrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
            Integer count = imUserDataMapper.selectCount(queryWrapper);
            Float percentage = ((float)count/(float)userCount) * 100;
            DecimalFormat df =new DecimalFormat("#.##");
            percentage = Float.valueOf(df.format(percentage));
            UserTypeResp userTypeResp = new UserTypeResp();
            userTypeResp.setRole(e.getName());
            userTypeResp.setPercentage(percentage);
            result.add(userTypeResp);
        });
        LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImUserDataEntity::getAppId,appId);
        queryWrapper.eq(ImUserDataEntity::getRoleId,0);
        queryWrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
        Integer integer = imUserDataMapper.selectCount(queryWrapper);
        Float percentage = ((float)integer/(float)userCount) * 100;
        UserTypeResp userTypeResp = new UserTypeResp();
        userTypeResp.setRole("非角色用户");
        userTypeResp.setPercentage(percentage);
        result.add(userTypeResp);

        return ResponseVO.successResponse(result);
    }

    @Override
    public ResponseVO getDataOfWeek(Integer appId) {
        Map<String,Object> result =new HashMap<>();
        List<Integer> actual = new ArrayList<>();
        List<Integer> expect = new ArrayList<>();


        List<String> weekDates = getNearlyWeekDates();
        weekDates.forEach(e->{
            String s = e + " 00:00:00";
            Timestamp startTime = Timestamp.valueOf(s);

            LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImUserDataEntity::getAppId,appId);
            queryWrapper.le(ImUserDataEntity::getCreateTime,startTime);
            Integer integer = imUserDataMapper.selectCount(queryWrapper);

            actual.add(integer);
            expect.add(initAndGet((double)integer));

        });
        result.put("date", weekDates);
        result.put("actual", actual);
        result.put("expect", expect);
        return ResponseVO.successResponse(result);
    }

    /**
     * 最近一周的所有日期
     * @return
     */
    public static List<String> getNearlyWeekDates() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        //过去七天
        c.setTime(new Date());
        String today = format.format(new Date());
        c.add(Calendar.DATE, - 7);
        Date d = c.getTime();
        String day = format.format(d);
        List<String> result = getBetweenDates(day,today,false);

        return result;
    }

    /**
     * 补全给定起止时间区间内的所有日期
     * @param startTime
     * @param endTime
     * @param isIncludeStartTime
     * @return
     */
    public static List<String> getBetweenDates(String startTime, String endTime,boolean isIncludeStartTime){
        List<String> result = new ArrayList<>();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date d1 = new SimpleDateFormat("yyyy-MM-dd").parse(startTime);//定义起始日期
            Date d2 = new SimpleDateFormat("yyyy-MM-dd").parse(endTime);//定义结束日期  可以去当前月也可以手动写日期。
            Calendar dd = Calendar.getInstance();//定义日期实例
            dd.setTime(d1);//设置日期起始时间
            if(isIncludeStartTime) {
                result.add(format.format(d1));
            }
            while (dd.getTime().before(d2)) {//判断是否到结束日期
                dd.add(Calendar.DATE, 1);//进行当前日期加1
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String str = sdf.format(dd.getTime());
                result.add(str);
//                System.out.println(str);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public ResponseVO adminLogin(HttpServletRequest request, AdminLoginReq req) {
        LambdaQueryWrapper<ImUserDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImUserDataEntity::getUserId,req.getUserId());
        queryWrapper.eq(ImUserDataEntity::getPassword,req.getPassword());
        queryWrapper.eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity userData = imUserDataMapper.selectOne(queryWrapper);

        if(userData != null){
            LambdaQueryWrapper<ImRoleEntity> query = new LambdaQueryWrapper<>();
            query.eq(ImRoleEntity::getRoleId,userData.getRoleId());

            ImRoleEntity imRoleEntity = imRoleMapper.selectOne(query);
            if(imRoleEntity == null || (imRoleEntity != null && (!RoleTypeEnum.SUPER_ADMIN.getValue().equals(imRoleEntity.getName()) && !RoleTypeEnum.ADMIN.getValue().equals(imRoleEntity.getName()) ))){
                return ResponseVO.errorResponse(500,"非管理员用户无法登录后台系统");
            }
            userData.setStatus(1);
            userData.getParam().put("role",imRoleEntity);
            imUserDataMapper.updateById(userData);

            storeLog(request,10000,req.getUserId(),1,"登录");

            return ResponseVO.successResponse(userData);
        }else{
            return ResponseVO.errorResponse(500,"登录失败,用户名或者密码错误");
        }
    }

    @Override
    public ResponseVO adminLogout(HttpServletRequest request,String userId) {
        LambdaQueryWrapper<ImUserDataEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImUserDataEntity::getUserId,userId);

        ImUserDataEntity userData = imUserDataMapper.selectOne(query);
        if(ObjectUtils.isNotEmpty(userData)){
            userData.setStatus(0);
            imUserDataMapper.updateById(userData);
            storeLog(request,10000,userId,1,"退出登录");
            return ResponseVO.successResponse();
        }
        return ResponseVO.errorResponse();
    }


    @Override
    public ResponseVO updateUserStatus(HttpServletRequest request,String userId, int status,String operator) {
        ImUserDataEntity imUserData = imUserDataMapper.selectById(userId);
        imUserData.setForbiddenFlag(status);
        imUserDataMapper.updateById(imUserData);
        if(imUserData.getStatus() == 1 && status == 0){
            messageProducer.sendToUser(userId, UserEventCommand.ADMIN_NOTIFY,imUserData,imUserData.getAppId());
        }
        if(status == 0){
            storeLog(request,10000,operator,1,"禁用用户");
        }else {
            storeLog(request,10000,operator,1,"启用用户");
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO createUser(HttpServletRequest request, ModifyUserInfoReq req) {

        ImUserDataEntity userData = new ImUserDataEntity();
        BeanUtils.copyProperties(req,userData);

        imUserDataMapper.insert(userData);
        storeLog(request,req.getAppId(),req.getOperator(),1,"创建用户");
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO uploadLogo(HttpServletRequest request,MultipartFile uploadFile, String userId, Integer appId) throws IOException {
        ImUserDataEntity userData = imUserDataMapper.selectById(userId);
        if(ObjectUtils.isEmpty(userData)){
            return ResponseVO.errorResponse(500,"操作用户不存在");
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
            String name =  userId + suffix;
            String fileName =  logoUploadPath + userId + suffix;
            String adminFileName = logoAdminPath + userId + suffix;
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
                    storeLog(request, appId, userId, 1, "用户头像修改");
                } else {
                    storeLog(appId, userId, 1, "用户头像修改");
                }


                //返回成功结果，附带文件的相对路径
                String picture = logoPath + name;

                LambdaQueryWrapper<ImMessageFileEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImMessageFileEntity::getName,name);
                wrapper.eq(ImMessageFileEntity::getLocation,picture);
                wrapper.eq(ImMessageFileEntity::getType,1);
                ImMessageFileEntity imMessageFileEntity = imMessageFileMapper.selectOne(wrapper);
                if(ObjectUtils.isEmpty(imMessageFileEntity)){
                    ImMessageFileEntity fileEntity = new ImMessageFileEntity();
                    fileEntity.setType(1);
                    fileEntity.setName(name);
                    fileEntity.setLocation(picture);
                    fileEntity.setSize(uploadFile.getSize());
                    fileEntity.setOperatorId(userId);
                    fileEntity.setExtension(suffix);
                    imMessageFileMapper.insert(fileEntity);
                }

                if (!userData.getPhoto().equals(picture)){
                    userData.setPhoto(picture);
                    imUserDataMapper.updateById(userData);
                }
                return ResponseVO.successResponse(picture);
            } catch (IOException e) {
                e.printStackTrace();
                if (isAdmin == 1) {
                    storeLog(request, appId, userId, 0, "用户头像修改");
                } else {
                    storeLog(appId, userId, 0, "用户头像修改");
                }
                return ResponseVO.errorResponse(500, "上传失败");
            }

        }
        if (isAdmin == 1){
            storeLog(appId,userId,0,"用户头像修改");
        }else {
            storeLog(appId,userId,0,"用户头像修改");
        }
        System.out.println("上传的文件为空");
        return ResponseVO.errorResponse(500, "上传的文件为空");
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

    private void storeLog(Integer appId, String userId ,int status,String text) {
        SysLog sysLog = new SysLog();
        Object obj = redisTemplate.opsForHash().get(appId + Constants.RedisConstants.UserSessionConstants + userId, "1:windows");
        JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
        sysLog.setUserId(object.getString("userId"));
        sysLog.setAppId(Integer.valueOf(object.getString("appId")));
        sysLog.setBrokerHost(object.getString("brokerHost"));
        sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
        sysLog.setImei(object.getString("imei"));
        sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));
        sysLog.setClientType(Integer.valueOf(object.getString("clientType")));
        sysLog.setStatus(status);
        sysLog.setTime(new Timestamp(new Date().getTime()));
        sysLog.setOperate(text);
        //存储log
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
    }

}
