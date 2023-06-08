import request from '../utils/request';

// 角色分页查询
export function getRoleByPage(roleReq) {
    return request({
      url: 'v1/role/getByPage',
      method: 'post',
      data: roleReq
    })
  }


//角色状态改变
export function changeStatus(roleId,status,operator) {
    return request({
      url: 'v1/role/changeStatus/'+ roleId + '/' + status + '/' + operator,
      method: 'put',
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



