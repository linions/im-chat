package com.lld.im.service.system.model.req;


import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class GetParamsReq extends RequestBase {

    private Integer page;

    private Integer pageSize;

    private String name;

    private Integer isSys;

    List<Date> createTime;

}
