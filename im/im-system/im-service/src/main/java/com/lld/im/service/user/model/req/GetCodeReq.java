package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
public class GetCodeReq extends RequestBase {

   private  String mobile;

   private String email;

   private int type;
}
