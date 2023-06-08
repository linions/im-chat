package com.lld.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;


@Data
@TableName("im_group_member")
public class ImGroupMemberEntity {

    @TableId(type = IdType.AUTO)
    private Long groupMemberId;

    private Integer appId;

    private String groupId;

    //成员id
    private String memberId;

    //群成员类型，0 普通成员, 1 管理员, 2 群主， 3 禁言，4 已经移除的成员
    private Integer role;

    private Date speakDate;

    private int mute;

    //群昵称
    private String alias;

    //加入时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp joinTime;

    //离开时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp leaveTime;

    private String joinType;

    private String extra;
}
