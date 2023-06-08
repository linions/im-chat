package com.lld.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;

    /** messageBodyId*/
    @TableId
    private String messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private int type;

    @TableField(value = "file_id")
    private Long fileId;

    private String extra;

    @JsonFormat(pattern = "M-dd HH:mm")
    private Timestamp messageTime;

    @JsonFormat(pattern = "M-dd HH:mm")
    private Timestamp endTime;

    private Integer delFlag;

    @TableField(exist = false)
    private Map<String,Object> param = new HashMap<>();

}
