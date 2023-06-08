package com.lld.im.service.friendship.model.req;


import com.lld.im.common.model.RequestBase;
import lombok.Data;

@Data
public class SearchFriendReq extends RequestBase {

    private String userId;

    private String searchInfo;

}