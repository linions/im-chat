package com.lld.im.service.system.model.req;


import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class ParamsReq extends RequestBase {

    private String name;

    private String paramKey;

    private String paramValue;

    private Integer isSys;

    private String remark;

}
