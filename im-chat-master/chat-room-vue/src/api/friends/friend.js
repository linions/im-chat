import request from '../../utils/request'


// 好友列表
export function friendsList(allFriendShipReq) {
  return request({
    url: '/v1/friendship/getAllFriendShip',
    method: 'post',
    data: allFriendShipReq
  })
}



// 好友关系
export function getRelation(getRelationReq) {
  return request({
    url: '/v1/friendship/getRelation',
    method: 'post',
    data: getRelationReq
  })
}



// 好友关系
export function getRelationWithInfo(getRelationReq) {
  return request({
    url: '/v1/friendship/getRelationWithInfo',
    method: 'post',
    data: getRelationReq
  })
}

// 校验好友关系
export function checkFriendShip(checkFriendShipReq) {
  return request({
    url: '/v1/friendship/checkFriend',
    method: 'post',
    data: checkFriendShipReq
  })
}



// 查找好友
export function findFriends(searchInfo) {
  return request({
    url: '/v1/friendship/searchFriend',
    method: 'post',
    data: searchInfo
  })
}

// 添加好友
export function addFriend(data) {
  return request({
    url: '/business/friends/addFriend',
    method: 'post',
    data: data
  })
}

// 修改好友备注
export function updateNote(updateReq) {
  return request({
    url: '/v1/friendship/updateFriend',
    method: 'post',
    data: updateReq
  })
}

// 拉黑好友
export function blackFriend(blackFriendReq) {
  return request({
    url: '/v1/friendship/addBlack',
    method: 'post',
    data: blackFriendReq
  })
}

// 取消拉黑好友
export function cancelBlackFriend(cancelBlackReq) {
  return request({
    url: '/v1/friendship/deleteBlack',
    method: 'post',
    data: cancelBlackReq
  })
}

// 删除好友
export function deleteFriend(deleteFriendReq) {
  return request({
    url: '/v1/friendship/deleteFriend',
    method: 'post',
    data: deleteFriendReq
  })
}

// 删除头像
export function uninstallLogo(fileName) {
  return request({
    url: '/v1/user/uninstallLogo/'+ fileName,
    method: 'delete',
  })
}