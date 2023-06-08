import request from '../../utils/request'


//获取群成员信息
export function getMemberById(groupId,appId,memberId) {
  return request({
    url: 'v1/group/member/getMemberById/' + groupId + "/" + appId + "/" + memberId,
    method: 'get',
  })
}


// 群聊列表
export function groupList(getGroupReq) {
  return request({
    url: '/v1/group/getJoinedGroup',
    method: 'post',
    data: getGroupReq
  })
}

// 获取群信息
export function groupInfo(groupInfoReq) {
  return request({
    url: '/v1/group/getGroupInfo',
    method: 'post',
    data: groupInfoReq
  })
}


// 搜索群聊
export function searchGroup(searchGroupReq) {
  return request({
    url: '/v1/group/searchGroup',
    method: 'post',
    data: searchGroupReq
  })
}

// 添加群聊
export function ad(data) {
  return request({
    url: '/business/groupUser/addGroup',
    method: 'post',
    data: data
  })
}

// 创建群聊
export function createGroup(createGroupReq) {
  return request({
    url: '/v1/group/createGroup',
    method: 'post',
    data: createGroupReq
  })
}

// 删除群头像
export function uninstallLogo(fileName) {
  return request({
    url: '/v1/group/uninstallLogo/'+ fileName,
    method: 'delete',
  })
}

// 修改群信息
export function updateGroup(updateReq) {
  return request({
    url: '/v1/group/updateGroup',
    method: 'post',
    data:updateReq
  })
}

// 修改群员信息
export function updateGroupMember(updateMemberReq) {
  return request({
    url: '/v1/group/member/update',
    method: 'post',
    data:updateMemberReq
  })
}

// 邀请成员
export function addGroupMember(addMemberReq) {
  return request({
    url: '/v1/group/member/add',
    method: 'post',
    data:addMemberReq
  })
}

// 退出群聊
export function exitGroupMember(exitMemberReq) {
  return request({
    url: '/v1/group/member/exit',
    method: 'post',
    data:exitMemberReq
  })
}

// 解散群聊
export function destroyGroup(destroyGroupReq) {
  return request({
    url: '/v1/group/destroyGroup',
    method: 'post',
    data:destroyGroupReq
  })
}

// 转让群主
export function transferGroup(transferGroupReq) {
  return request({
    url: '/v1/group/transferGroup',
    method: 'post',
    data:transferGroupReq
  })
}



// 踢出成员
export function removeGroupMember(removeGroupMemberReq) {
  return request({
    url: 'v1/group/member/remove',
    method: 'post',
    data:removeGroupMemberReq
  })
}

// 禁言成员
export function muteGroupMember(muteGroupMemberReq) {
  return request({
    url: 'v1/group/member/speak',
    method: 'post',
    data:muteGroupMemberReq
  })
}
