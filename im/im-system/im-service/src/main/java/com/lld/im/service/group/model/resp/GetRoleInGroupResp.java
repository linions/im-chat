package com.lld.im.service.group.model.resp;

import lombok.Data;

import java.util.Date;

@Data
public class GetRoleInGroupResp {

    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Date speakDate;

    private int mute;

}
