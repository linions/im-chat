package com.lld.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.SysLog;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.MessageReadContent;
import com.lld.im.common.model.message.MessageReciveAckContent;
import com.lld.im.common.model.message.RecallMessageContent;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.user.model.UserStatusChangeNotifyContent;
import com.lld.im.service.user.service.ImUserStatusService;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

@Component
public class ChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ImUserDataMapper userDataMapper;

    @Autowired
    ImUserStatusService imUserStatusService;


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2MessageService, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService, durable = "true")
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            logger.info("command :: {}", command);

            if (command.equals(SystemCommand.LOGOUT.getCommand())) {
//                退出登录
                SysLog sysLog = jsonObject.toJavaObject(SysLog.class);
                Object obj = redisTemplate.opsForHash().get(sysLog.getAppId() + Constants.RedisConstants.UserSessionConstants + sysLog.getUserId(), sysLog.getClientType() + ":" + sysLog.getImei());
                JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
                sysLog.setBrokerHost(object.getString("brokerHost"));
                sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
                sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));


                LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImUserDataEntity::getAppId,sysLog.getAppId());
                wrapper.eq(ImUserDataEntity::getUserId,sysLog.getUserId());
                wrapper.eq(ImUserDataEntity::getStatus,1);
                ImUserDataEntity userData = userDataMapper.selectOne(wrapper);
                if(ObjectUtils.isNotEmpty(userData)){
                    userData.setStatus(0);
                    userDataMapper.update(userData,wrapper);
                    //            存储登录log
                    sysLog.setStatus(1);
                    sysLog.setTime(new Timestamp(new Date().getTime()));
                    sysLog.setOperate("退出登录");
                    redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
                    UserStatusChangeNotifyContent content = new UserStatusChangeNotifyContent();
                    content.setStatus(0);
                    content.setAppId(userData.getAppId());
                    content.setUserId(userData.getUserId());
                    imUserStatusService.processUserOnlineStatusNotify(content);
                }
            }else if (command.equals(SystemCommand.LOGIN.getCommand())) {
//                登录
                SysLog sysLog = jsonObject.toJavaObject(SysLog.class);
                Object obj = redisTemplate.opsForHash().get(sysLog.getAppId() + Constants.RedisConstants.UserSessionConstants + sysLog.getUserId(), sysLog.getClientType() + ":" + sysLog.getImei());
                JSONObject object = JSONObject.parseObject(JSON.toJSONString(obj));
                sysLog.setBrokerHost(object.getString("brokerHost"));
                sysLog.setBrokerId(Integer.valueOf(object.getString("brokerId")));
                sysLog.setConnectState(Integer.valueOf(object.getString("connectState")));

                LambdaQueryWrapper<ImUserDataEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ImUserDataEntity::getAppId,sysLog.getAppId());
                wrapper.eq(ImUserDataEntity::getUserId,sysLog.getUserId());
                wrapper.eq(ImUserDataEntity::getStatus,0);
                ImUserDataEntity userData = userDataMapper.selectOne(wrapper);
                if(ObjectUtils.isNotEmpty(userData)){
                    userData.setStatus(1);
                    userDataMapper.update(userData,wrapper);
                    //            存储登录log
                    sysLog.setStatus(1);
                    sysLog.setTime(new Timestamp(new Date().getTime()));
                    sysLog.setOperate("登录");
                    redisTemplate.opsForZSet().add(sysLog.getAppId() + ":" + Constants.RedisConstants.SysLogConstants,JSONObject.toJSONString(sysLog),new Date().getTime());
                    UserStatusChangeNotifyContent content = new UserStatusChangeNotifyContent();
                    content.setStatus(1);
                    content.setAppId(userData.getAppId());
                    content.setUserId(userData.getUserId());
                    imUserStatusService.processUserOnlineStatusNotify(content);
                }
            }else if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                //处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                logger.info("CHAT MSG FORM QUEUE ::: {}", messageContent);
                p2PMessageService.process(messageContent);
            } else if (command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
                //消息接收确认
                MessageReciveAckContent messageContent = jsonObject.toJavaObject(MessageReciveAckContent.class);
                messageSyncService.receiveMark(messageContent);
            } else if (command.equals(MessageCommand.MSG_READED.getCommand())) {
                //消息接收确认
                MessageReadContent messageContent = jsonObject.toJavaObject(MessageReadContent.class);
                messageSyncService.readMark(messageContent);
            } else if (command.equals(MessageCommand.MSG_RECALL.getCommand())) {
//                撤回消息
                RecallMessageContent messageContent = JSON.parseObject(msg, new TypeReference<RecallMessageContent>() {
                }.getType());
                messageSyncService.recallMessage(messageContent);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }

    }


}
