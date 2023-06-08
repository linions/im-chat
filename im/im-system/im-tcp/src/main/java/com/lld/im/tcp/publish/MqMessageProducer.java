package com.lld.im.tcp.publish;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.CommandType;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MqMessageProducer {

    public static void sendMessage(Message message,Integer command){
        Channel channel;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if(commandType == CommandType.MESSAGE){
            channelName = Constants.RabbitConstants.Im2MessageService;
        }else if(commandType == CommandType.GROUP){
            channelName = Constants.RabbitConstants.Im2GroupService;
        }else if(commandType == CommandType.FRIEND){
            channelName = Constants.RabbitConstants.Im2FriendshipService;
        }else if(commandType == CommandType.USER){
            channelName = Constants.RabbitConstants.Im2UserService;
        }else if(commandType == CommandType.MEDIA){
            channelName = Constants.RabbitConstants.Im2MediaService;
        }

        try {
            channel = MqFactory.getChannel(channelName);
            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command",command);
            o.put("clientType",message.getMessageHeader().getClientType());
            o.put("imei",message.getMessageHeader().getImei());
            o.put("appId",message.getMessageHeader().getAppId());
            log.info("发送消息：{}",channelName);
            channel.basicPublish(channelName,"",null, o.toJSONString().getBytes());
        }catch (Exception e){
            log.error("发送消息出现异常：{}",e.getMessage());
        }
    }

    public static void sendMessage(Object message, UserSession userSession, Integer command){
        Channel channel;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if(commandType == CommandType.MESSAGE){
            channelName = Constants.RabbitConstants.Im2MessageService;
        }else if(commandType == CommandType.GROUP){
            channelName = Constants.RabbitConstants.Im2GroupService;
        }else if(commandType == CommandType.FRIEND){
            channelName = Constants.RabbitConstants.Im2FriendshipService;
        }else if(commandType == CommandType.USER){
            channelName = Constants.RabbitConstants.Im2UserService;
        }else if(commandType == CommandType.MEDIA){
            channelName = Constants.RabbitConstants.Im2MediaService;
        }

        try {
            channel = MqFactory.getChannel(channelName);
            JSONObject o = (JSONObject) JSON.toJSON(message);
            o.put("command",command);
            o.put("clientType",userSession.getClientType());
            o.put("imei",userSession.getImei());
            o.put("appId",userSession.getAppId());
            o.put("brokeHost",userSession.getBrokerHost());
            o.put("connectState",userSession.getConnectState());
            log.info("发送消息：{}",channelName);

            channel.basicPublish(channelName,"", null, o.toJSONString().getBytes());            log.info("发送消息：{}",channelName);

        }catch (Exception e){
            log.error("发送消息出现异常：{}",e.getMessage());
        }
    }

}
