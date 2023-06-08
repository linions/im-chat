package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.group.dao.ImGroupRequestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ImGroupRequestMapper extends BaseMapper<ImGroupRequestEntity> {

    @Update("update  im_group_request\n" +
            "set del_flag = 1\n" +
            "where  member_id = #{userId} and del_flag = 0")
    void destroyUser(String userId);
}
