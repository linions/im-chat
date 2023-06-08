package com.lld.im.service.group.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */

@Data
public class GroupMemberDto extends RequestBase {

    private String photo;

    private String memberId;

    private String alias;

    private Integer role;//群成员类型，0 普通成员, 1 管理员, 2 群主， 3 已经移除的成员，当修改群成员信息时，只能取值0/1，其他值由其他接口实现，暂不支持3

    private int mute;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date speakDate;

    private String joinType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date joinTime;

    private Integer isFriend = 0;

    private String addWording;

}
