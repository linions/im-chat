package com.lld.im.service.system.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;


@Data
public class HandleNotifyReq extends RequestBase {

    private Integer id;

   private String feedBack;

}
