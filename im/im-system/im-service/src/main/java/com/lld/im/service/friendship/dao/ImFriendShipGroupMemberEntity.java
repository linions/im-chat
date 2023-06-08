package com.lld.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("im_friendship_group_member")
public class ImFriendShipGroupMemberEntity {

    private Long groupId;


    private String toId;

}
