package com.lld.im.common.model.message;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

import java.util.Date;


@Data
public class MediaContent{

    private String type;

    private Object sdp;

    private Object candidate;

}
