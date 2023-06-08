package com.lld.im.common.model.message;

import lombok.Data;

@Data
public class FileDto {

    private String name;    //文件名称

    private Integer command; // 1请求创建文件 2传输文件

    private byte[] bytes;       //文件字节；再实际应用中可以使用非对称加密，以保证传输信息安全

    private String url;

    private String size;

    private int fileType;

}
