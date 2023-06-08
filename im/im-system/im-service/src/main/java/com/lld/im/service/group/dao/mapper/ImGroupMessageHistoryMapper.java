package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.model.req.GetGroupMessageReq;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.model.req.GetMessageReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImGroupMessageHistoryMapper extends BaseMapper<ImGroupMessageHistoryEntity> {

    @Select("select *\n" +
            "from im_group_message_history im \n" +
            "where app_id = #{appId} and group_id=#{groupId}  and sequence = (SELECT max(sequence)\n" +
            "from im_group_message_history\n" +
            "where group_id= im.group_id \n" +
            "group by group_id)")
    ImGroupMessageHistoryEntity getUpdatedMessage(GetGroupMessageReq req);

    @Select("select *\n" +
            "from im_group_message_history im \n" +
            "where app_id = #{appId} and group_id=#{groupId}\n" +
            "order by message_time desc\n" +
            "limit 1"
    )
    ImGroupMessageHistoryEntity getUpdatedMessageByTime(GetGroupMessageReq req);

    @Select("select *\n" +
            "from im_group_message_history\n" +
            "where app_id = #{appId} and group_id = #{groupId}" +
            "order by message_time asc")
    List<ImGroupMessageHistoryEntity> selectMessage(GetGroupMessageReq req);
}
