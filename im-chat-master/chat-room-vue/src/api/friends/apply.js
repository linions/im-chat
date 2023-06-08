import request from '../../utils/request'

// 申请列表
export function applyList(friendShipRequestReq) {
  return request({
    url: 'v1/friendshipRequest/getFriendRequest',
    method: 'post',
    data: friendShipRequestReq
  })
}


// 发送申请
export function sendApply(applyFriendData) {
  return request({
    url: '/v1/friendship/addFriend',
    method: 'post',
    data: applyFriendData,
  })
}

// 发送申请
export function approveApply(approveApplyReq) {
  return request({
    url: '/v1/friendshipRequest/approveFriendRequest',
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