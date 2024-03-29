package com.lld.im.service.group.model.callback;

import com.lld.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class AddMemberAfterCallback {
    private String groupId;
    private Integer groupType;
    private String operator;
    private List<AddMemberResp> memberId;
}
