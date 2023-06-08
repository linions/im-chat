package com.lld.message.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;


@Data
@TableName("im_message_file")
public class ImMessageFileEntity {

    @TableId(type = IdType.AUTO)
    private Long fileId;

    private String name;

    private String location;

    private float size;

    private String extension;

    private int type;

    private String operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
