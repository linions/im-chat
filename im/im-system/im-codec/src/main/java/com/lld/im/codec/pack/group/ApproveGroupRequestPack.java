package com.lld.im.codec.pack.group;

import lombok.Data;


@Data
public class ApproveGroupRequestPack {

    private Long id;

    //1同意 2拒绝
    private Integer status;

    private Long sequence;
}
