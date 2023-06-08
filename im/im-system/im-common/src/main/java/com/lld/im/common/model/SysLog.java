package com.lld.im.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;


@Data
public class SysLog {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    //连接状态 1=在线 2=离线
    private Integer connectState;

    private Integer brokerId;

    private String brokerHost;

    private String imei;

    private String operate;

    //操作成功或者失败
    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp time;

}
