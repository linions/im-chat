package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.model.req.GroupMemberDto;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface ImGroupMemberMapper extends BaseMapper<ImGroupMemberEntity> {

    @Select("select group_id from im_group_member where app_id = #{appId} AND member_id = #{memberId} and role != 3")
    public List<String> getJoinedGroupId(Integer appId, String memberId);

    @Select("select group_id from im_group_member where app_id = #{appId} AND member_id = #{memberId} and role != #{role}" )
    public List<String> syncJoinedGroupId(Integer appId, String memberId, int role);


    @Results({
            @Result(column = "member_id", property = "memberId"),
//            @Result(column = "speak_flag", property = "speakFlag"),
            @Result(column = "speak_date", property = "speakDate"),
            @Result(column = "role", property = "role"),
            @Result(column = "alias", property = "alias"),
            @Result(column = "join_time", property = "joinTime"),
            @Result(column = "join_type", property = "joinType")
    })
    @Select("select " +
            " member_id, " +
            " mute,  " +
            " speak_date,  " +
            " role, " +
            " alias, " +
            " join_time ," +
            " join_type " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} and role != 3" +
            " order by role desc, alias asc")
    public List<GroupMemberDto> getGroupMember(Integer appId, String groupId);

    @Select("select " +
            " member_id " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} and role != 3")
    public List<String> getGroupMemberId(Integer appId, String groupId);


    @Results({
            @Result(column = "member_id", property = "memberId"),
//            @Result(column = "speak_flag", property = "speakFlag"),
            @Result(column = "role", property = "role")
//            @Result(column = "alias", property = "alias"),
//            @Result(column = "join_time", property = "joinTime"),
//            @Result(column = "join_type", property = "joinType")
    })
    @Select("select " +
            " member_id, " +
//            " speak_flag,  " +
            " role " +
//            " alias, " +
//            " join_time ," +
//            " join_type " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} and role in (1,2) ")
    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId);

    @Update("update  im_group_member\n" +
            "set role = 3\n" +
            "where  member_id = #{userId} ")
    void destroyUser(String userId);
}
