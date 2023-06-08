package com.lld.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MediaEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.MessageReadContent;
import com.lld.im.common.model.message.MessageReciveAckContent;
import com.lld.im.common.model.message.RecallMessageContent;
import com.lld.im.service.message.service.MediaChatService;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class MediaChatReceiver {

    private static Logger logger = LoggerFactory.getLogger(MediaChatReceiver.class);

    @Autowired
    MediaChatService mediaChatService;


    @RabbitListener(
            bindings = @QueueBinding(
                 value = @Queue(value = Constants.RabbitConstants.Im2MediaService,durable = "true"),
                 exchange = @Exchange(value = Constants.RabbitConstants.Im2MediaService,durable = "true")
            ),concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String,Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(),"utf-8");
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (command.equals(MediaEventCommand.CALL_VIDEO.getCommand())||command.equals(MediaEventCommand.CALL_VOICE.getCommand())) {
                //处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                logger.info("CHAT MSG FORM QUEUE ::: {}", messageContent);
                mediaChatService.process(messageContent);
            }else if(command.equals(MediaEventCommand.ACCEPT_CALL.getCommand())){
                //消息接收确认
                MessageReciveAckContent messageContent = jsonObject.toJavaObject(MessageReciveAckContent.class);
                mediaChatService.acceptCall(messageContent);
            }else if(command.equals(MediaEventCommand.REJECT_CALL.getCommand()) || command.equals(MediaEventCommand.CANCEL_CALL.getCommand())
                     || command.equals(MediaEventCommand.TIMEOUT_CALL.getCommand()) || command.equals(MediaEventCommand.HANG_UP.getCommand())){
                //通话处理
                MessageReciveAckContent messageContent = jsonObject.toJavaObject(MessageReciveAckContent.class);
                logger.info("CHAT MSG FORM QUEUE ::: {}", messageContent);
                mediaChatService.handle(messageContent);
            }else if(command.equals(MediaEventCommand.TRANSMIT_OFFER.getCommand()) || command.equals(MediaEventCommand.TRANSMIT_ANSWER.getCommand())){
                MessageReciveAckContent messageContent = jsonObject.toJavaObject(MessageReciveAckContent.class);
                logger.info("CHAT MSG FORM QUEUE ::: {}", messageContent);
                mediaChatService.handleAck(messageContent);
            }
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, true);
        }

    }


}
