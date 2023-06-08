import request from '../utils/request'


/**
 * 发送通知
 * @param {*} data
 * @returns
 */
export function sendNotify(data) {
  return request({
    url: '/v1/system/notify/create',
    method: 'post',
    data: data,
  })
}

/**
 * 发送通知
 * @param {*} data
 * @returns
 */
export function getNotify(data) {
  return request({
    url: '/v1/system/notify/getByPage',
    method: 'post',
    data: data,
  })
}

