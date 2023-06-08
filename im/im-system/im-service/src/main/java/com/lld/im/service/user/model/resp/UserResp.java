package com.lld.im.service.user.model.resp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lld.im.common.model.RequestBase;
import com.lld.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

@Data
public class UserResp extends RequestBase {

    private int sumCount;

    private IPage page;

}
