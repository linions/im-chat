package com.lld.im.service.friendship.model.resp;

import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import lombok.Data;

import java.util.List;

/**
 * @className: GetFriendRequestResp
 * @author: linion
 * @date: 2023/3/27 17:39
 * @version: 1.0
 */

@Data
public class GetFriendRequestResp {

    private List<ImFriendShipRequestEntity> fromList;

    private List<ImFriendShipRequestEntity> toList;

}
