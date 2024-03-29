package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @className: ExitGroupReq
 * @author: linion
 * @date: 2023/3/13 15:49
 * @version: 1.0
 */

@Data
public class ExitGroupReq extends RequestBase {
    @NotBlank(message = "群id不能为空")
    private String groupId;

//    @NotEmpty(message = "群成员不能为空")
    private List<GroupMemberDto> members;
}
