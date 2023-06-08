package com.lld.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author: linion
 * @description:
 **/
@Data
public class SyncResp<T> {

    private Long maxSequence;

    private boolean isCompleted;

    private List<T> dataList;

}
