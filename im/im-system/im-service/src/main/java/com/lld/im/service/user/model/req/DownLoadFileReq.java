package com.lld.im.service.user.model.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.message.FileDto;
import lombok.Data;


@Data
public class DownLoadFileReq extends RequestBase {


    private String name;    //文件名称

    private String url;    //文件名称

    private String downLoadUrl;

    private String reName;    //文件名称

    private int isAdmin;

}
