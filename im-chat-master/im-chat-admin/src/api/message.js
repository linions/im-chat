import request from '../utils/request';


// 文件分页查询
export function getMessageFileByPage(messageFileReq) {
  return request({
    url: 'v1/message/getMessageFileByPage',
    method: 'post',
    data: messageFileReq
  })
}


// 删除文件查询
export function deleteMsgFile(fileId,operator) {
  return request({
    url: 'v1/message/deleteMsgFile/' + fileId + "/" + operator,
    method: 'delete',
  })
}

// 消息分页查询
export function getMessageByPage(messageReq) {
  return request({
    url: '/v1/message/getMessageByPage',
    method: 'post',
    data: messageReq
  })
}

// 删除消息
export function deleteMessageByKey(appId,messageKey,operator) {
  return request({
    url: 'v1/message/delete/' +  appId + '/' + messageKey + "/" + operator,
    method: 'delete',
  })
}


// 折线图数据
export function getMessageWeekData(appId) {
  return request({
    url: '/v1/message/getDataOfWeek/' + appId,
    method: 'get',
  })
}

//修改角色
export function updateRoleById(updateRoleReq) {
    return request({
      url: 'v1/role/update/',
      method: 'post',
      data: updateRoleReq
    })
}

//删除角色
export function deleteRole(DeleteRoleReq) {
    return request({
      url: 'v1/role/deleteById',
      method: 'post',
      data: DeleteRoleReq
    })
}


//创建角色
export function createRole(roleReq) {
    return request({
      url: 'v1/role/create',
      method: 'post',
      data: roleReq
    })
}

//绑定角色
export function bindRole(bindRoleReq) {
    return request({
      url: 'v1/role/bindRole',
      method: 'post',
      data: bindRoleReq
    })
}



