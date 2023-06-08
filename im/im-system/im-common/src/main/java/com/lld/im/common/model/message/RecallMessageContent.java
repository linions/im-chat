package com.lld.im.common.model.message;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class RecallMessageContent extends ClientInfo {

    private String messageKey;

    private String fromId;

    private String toId;

    private Timestamp messageTime;

    private Long messageSequence;

    private Integer conversationType;


//    {
//        "messageKey":419455774914383872,
//            "fromId":"lld",
//            "toId":"lld4",
//            "messageTime":"1665026849851",
//            "messageSequence":2,
//            "appId": 10000,
//            "clientType": 1,
//            "imei": "web",
//    "conversationType":0
//    }
}
