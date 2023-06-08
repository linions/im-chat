package com.lld.im.common.utils;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;


@Slf4j
public class Base64Util {
    /**
     * @return 加密后的字符串的base64格式
     * @Description: 加密
     **/
    public static String enCode(String content, String key) {
        String message = content + key;
        return Base64.encodeBase64String(message.getBytes());
//        return new String(encode, StandardCharsets.UTF_8);
    }

    /**
     * @return 解密后的字符串
     * @Description: 解密
     **/
    public static String deCode(String content){
        byte[] decode = Base64.decodeBase64(content.getBytes());
        return new String(decode, StandardCharsets.UTF_8);
    }

    /**
     * @return 加密后的字符串
     * @Description: 加密
     **/
    public static String encode(String content){
        return Base64.encodeBase64String(content.getBytes());
//        return new String(encode, StandardCharsets.UTF_8);
    }

    public static String deCode(String content, String key) {
        byte[] decode = Base64.decodeBase64(content.getBytes());
        String result = new String(decode, StandardCharsets.UTF_8);
        return result.replace(key, "");
    }

    public static void main(String[] args) {
        String message = "hello";
        String key = "sahidhasi";
        String s = enCode(message, key);
        System.out.println(s);
        System.out.println(deCode(s,key));
    }

}