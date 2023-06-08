package com.lld.im.service.user.model.resp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

@Data
public class UserTypeResp  {

    private String role;

    private float percentage;

}
