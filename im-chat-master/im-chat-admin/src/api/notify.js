import request from '../utils/request'

/**
 * 创建通知
 */
export function createNotify(data) {
  return request({
    url: '/v1/system/notify/create',
    method: 'post',
    data: data,
  })
}

/**
 * 发送通知
 */
export function sendNotify(appId,id,operator) {
    return request({
      url: '/v1/system/notify/send/' + appId + '/' + id + "/" + operator,
      method: 'post',
    })
  }

  /**
 * 修改通知
 */
export function updateNotify(data) {
    return request({
        url: '/v1/system/notify/update',
        method: 'put',
        data: data,
    })
}

  /**
 * 处理通知
 */
  export function handleNotify(data) {
    return request({
        url: '/v1/system/notify/handle',
        method: 'put',
        data: data,
    })
}



/**
 * 分页获取通知
 */
export function getNotify(data) {
  return request({
    url: '/v1/system/notify/getByPage',
    method: 'post',
    data: data,
  })
}


/**
 * 删除通知
 */
export function deleteNotify(id,appId,operator) {
    return request({
      url: '/v1/system/notify/delete/' + id + '/' + appId + '/' + operator,
      method: 'delete',
    })
}

