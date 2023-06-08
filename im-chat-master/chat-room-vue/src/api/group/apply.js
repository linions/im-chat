import request from '../../utils/request'

// 申请列表
export function applyList(groupRequestReq) {
  return request({
    url: '/v1/groupRequest/getGroupRequest',
    method: 'post',
    data: groupRequestReq
  })
}


// 发送申请
export function sendApply(applyGroupData) {
  return request({
    url: '/v1/groupRequest/getGroupRequest',
    method: 'post',
    data: applyGroupData
  })
}

// 审批申请
export function approveApply(approveApplyReq) {
  return request({
    url: '/v1/groupRequest/approveGroupRequest',
    method: 'put',
    data: approveApplyReq
  })
}

// 查看申请
export function checkApply(data) {
  return request({
    url: '/business/apply/checkApply',
    method: 'post',
    data: data
  })
}

/**
 * 注册
 * @returns 
 */
export function register(chatusernickname,chatusername, password) {
  const data = {
    chatusernickname,
    chatusername,
    password
  }
  return request({
    url: '/business/friends/register',
    method: 'post',
    data: data
  })
}

// 已读处理 （好友申请已读）
export function read(readFriendShipRequestReq) {
  return request({
    url: '/v1/friendshipRequest/readFriendShipRequestReq',
    method: 'post',
    data: readFriendShipRequestReq
  })
}