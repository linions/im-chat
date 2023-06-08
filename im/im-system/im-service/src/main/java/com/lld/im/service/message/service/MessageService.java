package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.model.SysLog;
import com.lld.im.common.model.message.FileDto;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageFileEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageFileMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.message.model.req.GetMessageReq;
import com.lld.im.service.message.model.req.MessageFileReq;
import com.lld.im.service.message.model.req.MessageReq;
import com.lld.im.service.message.model.resp.GetMessageResp;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.model.req.DownLoadFileReq;
import com.lld.im.service.utils.ConversationIdGenerate;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.lld.im.common.utils.Base64Util.deCode;
import static com.lld.im.common.utils.LinearRegression.initAndGet;

@Service
public class MessageService {

    @Autowired
    private ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    private ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    private ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    private ImMessageFileMapper imMessageFileMapper;


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;


    @Value("${data.uploadPath}")
    private String uploadPath;

    @Value("${data.logoAdminPath}")
    private String logoAdminPath;

    @Value("${data.path}")
    private String dataPath;

    @Value("${data.downloadPath}")
    private String downloadPath;


    /**
     * 计算文件大小
     * @return 1GB
     * */
    public String getFileSize(float size) {
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        try {
            // 格式化小数
            DecimalFormat df = new DecimalFormat("0.00");
            String resultSize = "";
            if (size / GB >= 1) {
                //如果当前Byte的值大于等于1GB
                resultSize = df.format(size / (float) GB) + "GB";
            } else if (size / MB >= 1) {
                //如果当前Byte的值大于等于1MB
                resultSize = df.format(size / (float) MB) + "MB";
            } else if (size / KB >= 1) {
                //如果当前Byte的值大于等于1KB
                resultSize = df.format(size / (float) KB) + "KB";
            } else {
                resultSize = size + "B";
            }
            return resultSize;
        } catch (Exception e) {
            return null;
        }
    }


    //获取聊天记录
    public ResponseVO getChatMessage(GetMessageReq req) {
        List<GetMessageResp> respList = new ArrayList<>();

        List<ImMessageHistoryEntity> imMessageHistoryEntities = imMessageHistoryMapper.selectMessage(req);
        imMessageHistoryEntities.forEach(e->{
            GetMessageResp getMessageResp = new GetMessageResp();
            BeanUtils.copyProperties(e,getMessageResp);
            //封装信息体
            LambdaQueryWrapper<ImMessageBodyEntity> query = new LambdaQueryWrapper<>();
            query.eq(ObjectUtils.isNotEmpty(e.getAppId()),ImMessageBodyEntity::getAppId, e.getAppId());
            query.eq(ObjectUtils.isNotEmpty(e.getMessageKey()),ImMessageBodyEntity::getMessageKey,e.getMessageKey());
            ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(query);

            if(messageBody.getType() == 2 || messageBody.getType() == 3){
                ImMessageFileEntity imMessageFileEntity = imMessageFileMapper.selectById(messageBody.getFileId());
                FileDto fileDto = new FileDto();
                fileDto.setName(imMessageFileEntity.getName());
                fileDto.setUrl(imMessageFileEntity.getLocation());
                fileDto.setSize(getFileSize(imMessageFileEntity.getSize()));
                fileDto.setFileType(imMessageFileEntity.getType());
                getMessageResp.setFileDto(fileDto);
            }



            if(messageBody.getType() != 2 && messageBody.getType() != 3){
                String message = deCode(messageBody.getMessageBody(), messageBody.getSecurityKey());
                getMessageResp.setMessageBody(message);
            }else {
                getMessageResp.setMessageBody(messageBody.getMessageBody());
            }

            getMessageResp.setType(messageBody.getType());
            getMessageResp.setMessageKey(messageBody.getMessageKey());
            getMessageResp.setEndTime(messageBody.getEndTime());

            if(getMessageResp.getType() == 4 || getMessageResp.getType() == 5){
                if(getMessageResp.getMessageBody().equals("5")){
                    Timestamp messageTime = messageBody.getMessageTime();
                    Timestamp endTime = messageBody.getEndTime();
                    if(endTime.before(messageTime)){
                        getMessageResp.setTime("异常中断");
                    }else{
                        long t1 = endTime.getTime();
                        long t2 = messageTime.getTime();
                        int hours=(int) ((t1 - t2)/(1000*60*60));
                        int minutes=(int) (((t1 - t2)/1000-hours*(60*60))/60);
                        int second=(int) ((t1 - t2)/1000-hours*(60*60)-minutes*60);
                        String time = hours + ":" + minutes + ":" + second;
                        getMessageResp.setTime(time);
                    }
                }
            }

//            boolean isText = messageBody.getType() == 1 ? true:false;
//            boolean isFile = messageBody.getType() == 2 || messageBody.getType() == 3 ? true:false;
            if(StringUtils.isNotBlank(req.getSearch()) && req.getType() != 0){
                if(getMessageResp.getMessageBody().contains(req.getSearch()) && getMessageResp.getType() == req.getType() ){
                    respList.add(getMessageResp);
                }
            }else if(StringUtils.isBlank(req.getSearch()) && req.getType() != 0){
                if(getMessageResp.getType() == req.getType() ){
                    respList.add(getMessageResp);
                }
            }else {
                respList.add(getMessageResp);
            }

        });
        String key = req.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(req.getFromId(),req.getToId());
        long valueSeq = redisSeq.getValueSeq(key);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),Constants.SeqConstants.Message+":"+ req.getToId(),valueSeq);

        return ResponseVO.successResponse(respList);
    }

//    获取最新的聊天记录
    public ResponseVO<GetMessageResp> getUpdatedMessage(GetMessageReq req) {
        GetMessageResp getMessageResp = new GetMessageResp();
//        ImMessageHistoryEntity updatedMessage = imMessageHistoryMapper.getUpdatedMessage(req);
        ImMessageHistoryEntity updatedMessage = imMessageHistoryMapper.getUpdatedMessageByTime(req);
        if (ObjectUtils.isEmpty(updatedMessage)){
            return ResponseVO.successResponse(null);
        }
        BeanUtils.copyProperties(updatedMessage,getMessageResp);
        //封装信息体
        LambdaQueryWrapper<ImMessageBodyEntity> query = new LambdaQueryWrapper<>();
        query.eq(ObjectUtils.isNotEmpty(updatedMessage.getAppId()),ImMessageBodyEntity::getAppId, updatedMessage.getAppId());
        query.eq(ObjectUtils.isNotEmpty(updatedMessage.getMessageKey()),ImMessageBodyEntity::getMessageKey,updatedMessage.getMessageKey());

        ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(query);
        if(messageBody.getType() != 2 && messageBody.getType() != 3){
            String message = deCode(messageBody.getMessageBody(), messageBody.getSecurityKey());
            getMessageResp.setMessageBody(message);
        }else {
            getMessageResp.setMessageBody(messageBody.getMessageBody());
        }
        getMessageResp.setType(messageBody.getType());
        return ResponseVO.successResponse(getMessageResp);
    }

    public ResponseVO getMessageByPage(MessageReq req) {
            IPage<ImMessageBodyEntity> page = new Page<>(req.getPage(), req.getPageSize());

            LambdaQueryWrapper<ImMessageBodyEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.isNotBlank(req.getMessageKey()),ImMessageBodyEntity::getMessageKey, req.getMessageKey());
            wrapper.eq(ObjectUtils.isNotEmpty(req.getAppId()),ImMessageBodyEntity::getAppId, req.getAppId());
            wrapper.ne(ImMessageBodyEntity::getType, 4);
            wrapper.ne(ImMessageBodyEntity::getType,5);
            wrapper.eq(req.getType() != 0 ,ImMessageBodyEntity::getType, req.getType());
            if(!CollectionUtils.isEmpty(req.getCreateTime())){
                wrapper.between(ImMessageBodyEntity::getMessageTime,new Timestamp(req.getCreateTime().get(0).getTime()),new Timestamp(req.getCreateTime().get(1).getTime()));
            }
            wrapper.eq(ImMessageBodyEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

            IPage<ImMessageBodyEntity> ipage = imMessageBodyMapper.selectPage(page, wrapper);
            ipage.getRecords().forEach(e->{
                if(e.getType() != 2 && e.getType() != 3){
                    e.setMessageBody(deCode(e.getMessageBody(),e.getSecurityKey()));
                }else {
                    e.setMessageBody(e.getMessageBody());
                }
                if(e.getType() == 2 || e.getType() == 3){
                    LambdaQueryWrapper<ImMessageFileEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ImMessageFileEntity::getFileId, e.getFileId());
                    ImMessageFileEntity imMessageFileEntity = imMessageFileMapper.selectOne(queryWrapper);

                    String fileSize = getFileSize(imMessageFileEntity.getSize());
                    e.getParam().put("fileSize",fileSize);
                }
            });

            return ResponseVO.successResponse(ipage);
    }



    public ResponseVO getDataOfWeek(Integer appId) {
        Map<String,Object> result =new HashMap<>();
        List<Integer> actual = new ArrayList<>();
        List<Integer> expect = new ArrayList<>();

        List<String> weekDates = getNearlyWeekDates();
        weekDates.forEach(e->{
            String s1 = e + " 00:00:00";
            Timestamp startTime = Timestamp.valueOf(s1);
            Timestamp endTime;
            if(weekDates.indexOf(e) + 1 < weekDates.size()) {
                String s2 = weekDates.get(weekDates.indexOf(e) + 1) + " 00:00:00";
                endTime = Timestamp.valueOf(s2);
            }else{
                java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(new Date());
                calendar.add(calendar.DATE, +1);
                String tomorrow = sdf.format(calendar.getTime());
                endTime = Timestamp.valueOf(tomorrow);
            }

            LambdaQueryWrapper<ImMessageBodyEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImMessageBodyEntity::getAppId,appId);
            queryWrapper.between(ImMessageBodyEntity::getMessageTime,startTime,endTime);
            Integer integer = imMessageBodyMapper.selectCount(queryWrapper);

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
                System.out.println(str);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Transactional
    public ResponseVO deleteMessageByKey(HttpServletRequest request,Integer appId, String messageKey,String operator) {
        LambdaQueryWrapper<ImMessageBodyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessageBodyEntity::getMessageKey, messageKey);
        wrapper.eq(ImMessageBodyEntity::getAppId, appId);
        wrapper.eq(ImMessageBodyEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(wrapper);
        if (messageBody != null){
            messageBody.setDelFlag(DelFlagEnum.DELETE.getCode());
            imMessageBodyMapper.updateById(messageBody);
        }

        LambdaQueryWrapper<ImMessageHistoryEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImMessageHistoryEntity::getMessageKey, messageKey);
        query.eq(ImMessageHistoryEntity::getAppId, appId);
        query.eq(ImMessageHistoryEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        List<ImMessageHistoryEntity> imMessageHistoryEntities = imMessageHistoryMapper.selectList(query);
        if (imMessageHistoryEntities.size() > 0){
           imMessageHistoryEntities.forEach(e->{
               ImMessageHistoryEntity messageHistory = new ImMessageHistoryEntity();
               messageHistory.setDelFlag(DelFlagEnum.DELETE.getCode());
               imMessageHistoryMapper.update(messageHistory,query);
           });
        }

        LambdaQueryWrapper<ImGroupMessageHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImGroupMessageHistoryEntity::getMessageKey, messageKey);
        queryWrapper.eq(ImGroupMessageHistoryEntity::getAppId, appId);
        queryWrapper.eq(ImGroupMessageHistoryEntity::getDelFlag, DelFlagEnum.NORMAL.getCode());

        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = imGroupMessageHistoryMapper.selectOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(imGroupMessageHistoryEntity)){
            imGroupMessageHistoryEntity.setDelFlag(DelFlagEnum.DELETE.getCode());
            imGroupMessageHistoryMapper.updateById(imGroupMessageHistoryEntity);
        }

        if(messageBody.getType() == 2 || messageBody.getType() == 3){
            imMessageFileMapper.deleteById(messageBody.getFileId());
        }
        storeLog(request,appId,operator,1,"删除消息记录");
        return ResponseVO.successResponse();
    }

    public ResponseVO getMessageFileByPage(MessageFileReq req) {
        if(req.getPage() != 0 && req.getPageSize() != 0){
            IPage<ImMessageFileEntity> page = new Page(req.getPage(), req.getPageSize());
            LambdaQueryWrapper<ImMessageFileEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(StringUtils.isNotBlank(req.getFileName()),ImMessageFileEntity::getName, req.getFileName());
            queryWrapper.eq(req.getType() != 0,ImMessageFileEntity::getType, req.getType());
            queryWrapper.like(StringUtils.isNotBlank(req.getOperator()),ImMessageFileEntity::getOperatorId, req.getOperator());
            if(!CollectionUtils.isEmpty(req.getCreateTime())){
                queryWrapper.between(ImMessageFileEntity::getCreateTime,new Timestamp(req.getCreateTime().get(0).getTime()),new Timestamp(req.getCreateTime().get(1).getTime()));
            }
            IPage<ImMessageFileEntity> iPage = imMessageFileMapper.selectPage(page, queryWrapper);

            if (iPage.getRecords().size() > 0){
                iPage.getRecords().forEach(e->{
                    String fileSize = getFileSize(e.getSize());
                    e.getParam().put("fileSize",fileSize);
                });
            }
            return ResponseVO.successResponse(iPage);
        }else {
            return ResponseVO.successResponse(imMessageFileMapper.selectList(null));
        }

    }

    @Transactional
    public ResponseVO deleteMessageFileById(HttpServletRequest request,Integer fileId,String operator) {
        ImMessageFileEntity imMessageFileEntity = imMessageFileMapper.selectById(fileId);
        if(imMessageFileEntity.getType() == 3 || imMessageFileEntity.getType() == 4){
            LambdaQueryWrapper<ImMessageBodyEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImMessageBodyEntity::getFileId,fileId);
            queryWrapper.eq(ImMessageBodyEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

            ImMessageBodyEntity messageBody = imMessageBodyMapper.selectOne(queryWrapper);
            messageBody.setDelFlag(DelFlagEnum.DELETE.getCode());
            imMessageBodyMapper.updateById(messageBody);

            LambdaQueryWrapper<ImMessageHistoryEntity> query = new LambdaQueryWrapper<>();
            query.eq(ImMessageHistoryEntity::getMessageKey,messageBody.getMessageKey());
            query.eq(ImMessageHistoryEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

            List<ImMessageHistoryEntity> historyEntities = imMessageHistoryMapper.selectList(query);
            if(CollectionUtils.isEmpty(historyEntities)){
                historyEntities.forEach(e->{
                    e.setDelFlag(DelFlagEnum.DELETE.getCode());
                    imMessageHistoryMapper.updateById(e);
                });
            }

            LambdaQueryWrapper<ImGroupMessageHistoryEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImGroupMessageHistoryEntity::getMessageKey,messageBody.getMessageKey());
            wrapper.eq(ImGroupMessageHistoryEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());

            ImGroupMessageHistoryEntity groupMessageHistory = imGroupMessageHistoryMapper.selectOne(wrapper);
            if(ObjectUtils.isNotEmpty(groupMessageHistory)){
                groupMessageHistory.setDelFlag(DelFlagEnum.DELETE.getCode());
                imGroupMessageHistoryMapper.updateById(groupMessageHistory);
            }

        }
        imMessageFileMapper.deleteById(fileId);
        storeLog(request,10000,operator,1,"删除消息文件");
        return  ResponseVO.successResponse();
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
        redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
    }

    public ResponseVO uploadMsgFile(MultipartFile uploadFile) throws IOException {
        if ( uploadFile != null) {
            //获得上传文件的文件名
            String name = uploadFile.getOriginalFilename();
            String suffix = name.substring(name.indexOf('.'));
            System.out.println("[上传的文件名]：" + name);
            System.out.println("[上传的文件]：" + uploadFile.getBytes().length);
            //我的文件保存在static目录下的avatar/user
            String fileName = uploadPath + name;
            String adminFileName = logoAdminPath + name;
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

                String url = dataPath + name;

                Map<String,String> map = new HashMap<>();
                map.put("url",url);
                map.put("name",name);
                map.put("size", String.valueOf(uploadFile.getSize()));
                return ResponseVO.successResponse(map);
            } catch (IOException e) {
                return ResponseVO.errorResponse(500, "文件上传失败，请联系管理员");
            }
        }
        System.out.println("上传的文件为空");
        return ResponseVO.errorResponse(500, "上传的文件为空");
    }

    public ResponseVO downLoadFile(HttpServletRequest request, DownLoadFileReq req) {
        String name = uploadPath + req.getName();
        File file = new File(name);
        String reName;
        if(StringUtils.isBlank(req.getDownLoadUrl())){
            reName =  downloadPath + req.getName();
        }else {
            reName = req.getDownLoadUrl() + req.getName();
        }
        File downLoadFile = new File(reName);
        //如果文件不存在，创建文件
        if(!file.exists()){
            if (req.getIsAdmin() == 1){
                storeLog(request,req.getAppId(),req.getOperator(),0,"文件下载");
            }else {
                storeLog(req.getAppId(),req.getOperator(),0,"文件下载");
            }
            return ResponseVO.errorResponse(500,"文件下载失败");
        }
        if (!downLoadFile.exists()) {
            try {
                downLoadFile.createNewFile();
                //创建BufferedReader读取文件内容
                BufferedReader br = new BufferedReader(new FileReader(file));
                BufferedWriter bw = new BufferedWriter(new FileWriter(downLoadFile));
                String line;
                while ((line = br.readLine())!=null) {
                    //创建BufferedWriter对象并向文件写入内容
                    //向文件中写入内容
                    bw.write(line);
                    bw.flush();
                }
                br.close();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (req.getIsAdmin() == 1){
                    storeLog(request,req.getAppId(),req.getOperator(),0,"文件下载");
                }else {
                    storeLog(req.getAppId(),req.getOperator(),0,"文件下载");
                }
                return ResponseVO.errorResponse(500,"文件下载失败");
            }
            if (req.getIsAdmin() == 1){
                storeLog(request,req.getAppId(),req.getOperator(),1,"文件下载");
            }else {
                storeLog(req.getAppId(),req.getOperator(),1,"文件下载");
            }
            return ResponseVO.successResponse();
        }
        if (req.getIsAdmin() == 1){
            storeLog(request,req.getAppId(),req.getOperator(),0,"文件下载");
        }else {
            storeLog(req.getAppId(),req.getOperator(),0,"文件下载");
        }
        return ResponseVO.errorResponse(500,"文件已存在");
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
