package com.lld.im.service.system.model.req;


import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.sql.Date;
import java.util.List;

@Data
public class GetNotificationReq extends RequestBase {

    private Integer page;

    private Integer pageSize;

    private String fromId;

    private String toId;

    private int type = 0;

    private Integer status;

    List<Date> createTime;

}
