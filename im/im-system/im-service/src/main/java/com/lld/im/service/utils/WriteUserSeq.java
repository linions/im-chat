package com.lld.im.service.utils;

import com.lld.im.common.constant.Constants;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: linion
 * @version: 1.0
 */
@Service
public class WriteUserSeq {

    //redis
    //uid friend 10
    //    group 12
    //    conversation 123
    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId,String userId,String type,Long seq){
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }

    public void writeCode(Integer appId,String account,String code){
        String key = appId + ":" + Constants.RedisConstants.userCode + ":" + account;
        redisTemplate.opsForValue().getAndSet(key, code);
        redisTemplate.opsForValue().getOperations().expire(key,180, TimeUnit.SECONDS);

    }



}
