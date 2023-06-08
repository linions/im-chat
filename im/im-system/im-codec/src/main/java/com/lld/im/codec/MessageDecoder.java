package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx,ByteBuf in, List<Object> out) throws Exception {
    //请求头（指令
        // 版本
        // clientType
        // 消息解析类型
        // appId
        // imei长度
        // bodylen）+ imei号 + 请求体

        if(in.readableBytes() < 28){
            return;
        }
//        获取command
        int command = in.readInt();
//        获取version
        int version = in.readInt();
//        获取clientType
        int clientType = in.readInt();
//        获取messageType
        int messageType = in.readInt();
//        获取appId
        int appId = in.readInt();
//        获取imeiLength
        int imeiLength = in.readInt();
//        获取bodyLen
        int bodyLen = in.readInt();

        if(in.readableBytes() < bodyLen + imeiLength){
            in.resetReaderIndex();
            return;
        }

        byte [] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);


        byte [] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);



        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setCommand(command);
        messageHeader.setClientType(clientType);
        messageHeader.setImei(imei);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setLength(bodyLen);

        Message message = new Message();
        message.setMessageHeader(messageHeader);
        if (messageType == 0x0){
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }
        in.markReaderIndex();
        out.add(message);

//        Message message = ByteBufToMessageUtils.transition(in);
//        if(message == null){
//            return;
//        }
//

    }
}
