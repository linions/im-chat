import request from '../utils/request'

/**
 * 创建系统参数
 */
export function createParam(data) {
  return request({
    url: '/v1/system/param/create',
    method: 'post',
    data: data,
  })
}


  /**
 * 修改系统参数
 */
export function updateParam(data) {
    return request({
        url: '/v1/system/param/update',
        method: 'put',
        data: data,
    })
}


/**
 * 分页获取
 */
export function getParamByPage(data) {
  return request({
    url: '/v1/system/param/getByPage',
    method: 'post',
    data: data,
  })
}


/**
 * 删除系统参数
 */
export function deleteParam(id,appId,operator) {
    return request({
      url: '/v1/system/param/delete/' + id + '/' + appId + '/' + operator,
      method: 'delete',
    })
}

