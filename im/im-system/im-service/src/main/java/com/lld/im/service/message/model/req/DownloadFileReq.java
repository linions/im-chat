package com.lld.im.service.message.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.Date;

@Data
public class DownloadFileReq extends RequestBase {

    private String url;

    private String name;

    private String downloadUrl;

    private String fileName;



}
