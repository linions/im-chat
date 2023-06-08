package com.lld.im.service.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.model.req.GetMessageReq;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {

    /**
     * 批量插入（mysql）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);

    @Select("select *\n" +
            "from im_message_history im \n" +
            "where app_id = #{appId} and from_id=#{fromId} and to_id = #{toId} and owner_id= #{ownerId} and sequence = (SELECT max(sequence)\n" +
            "from im_message_history\n" +
            "where from_id= im.from_id and to_id = im.to_id\n" +
            "group by from_id,to_id)")
    ImMessageHistoryEntity getUpdatedMessage(GetMessageReq req);

    @Select("select *\n" +
            "from im_message_history im \n" +
            "where app_id = #{appId} and from_id=#{fromId} and to_id = #{toId} and owner_id= #{ownerId}\n" +
            "order by message_time desc\n" +
            "limit 1"
    )
    ImMessageHistoryEntity getUpdatedMessageByTime(GetMessageReq req);

    @Select("SELECT *\n" +
            "FROM im_message_history\n" +
            "where app_id= #{appId} and owner_id = #{fromId} and  ((from_id = #{fromId}  and to_id = #{toId}) or (from_id = #{toId}  and to_id = #{fromId}))" +
            "order by message_time asc")
    List<ImMessageHistoryEntity> selectMessage(GetMessageReq req);

    @Select("select  message_key\n" +
            "from im_message_history\n" +
            "where  ( from_id = #{userId} or to_id = #{userId}) and del_flag = 0")
    List<String> destroyUser(String userId);
}
