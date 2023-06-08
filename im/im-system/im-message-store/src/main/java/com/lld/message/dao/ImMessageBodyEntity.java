package com.lld.message.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    @TableField(value = "app_id")
    private Integer appId;

    /** messageBodyId*/
    @TableField(value = "message_key")
    private String messageKey;

    /** messageBody*/
    @TableField(value = "message_body")
    private String messageBody;

    @TableField(value = "security_key")
    private String securityKey;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date messageTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @TableField(value = "type")
    private Integer type;

    @TableField(value = "file_id")
    private Long fileId;

    @TableField(value = "extra")
    private String extra;

    @TableField(value = "del_flag")
    private Integer delFlag;

}
