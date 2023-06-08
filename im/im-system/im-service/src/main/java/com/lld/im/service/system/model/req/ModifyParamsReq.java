package com.lld.im.service.system.model.req;


import com.lld.im.common.model.RequestBase;
import lombok.Data;

@Data
public class ModifyParamsReq extends RequestBase {

    private Integer id;

    private String name;

    private String paramKey;

    private String paramValue;

    private Integer isSys;

    private String remark;

}
