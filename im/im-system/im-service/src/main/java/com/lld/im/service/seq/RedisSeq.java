package com.lld.im.service.seq;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSeq {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long doGetSeq(String key){
        return stringRedisTemplate.opsForValue().increment(key);
    }


    public long getValueSeq(String key){
        String s = stringRedisTemplate.opsForValue().get(key);
        long result = StringUtils.isBlank(s)  ? 0L : Long.parseLong(s);
        return result ;
    }

    public long getHashSeq(String hashKey,String key){
        Object obj = stringRedisTemplate.opsForHash().get(hashKey, key);
        long result = obj != null  ? Long.parseLong(obj.toString()) : 0L;
        return result ;
    }

}
