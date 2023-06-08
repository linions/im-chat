package com.lld.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ImFriendShipRequestMapper extends BaseMapper<ImFriendShipRequestEntity> {

    @Update("update  im_friendship_request\n" +
            "set del_flag = 1\n" +
            "where  ( from_id = #{userId} or to_id = #{userId}) and del_flag = 0")
    void destroyUser(String userId);
}
