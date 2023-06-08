package com.lld.im.codec.pack.friendship;

import lombok.Data;


@Data
public class ApproveFriendRequestPack {

    private Long id;

    //1同意 2拒绝
    private Integer status;

    private Long sequence;
}
