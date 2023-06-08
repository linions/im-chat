import request from '../../utils/request'


// 上传用户头像
export function uploadUserLogo(photo,userId,appId) {
  return request({
    url: '/v1/user/uploadLogo/'+ userId + '/'+appId,
    method: 'post',
    data: photo,
    headers: { "Content-Type": "multipart/form-data" },
  })
}

export function uninstallUserLogo(fileName) {
  return request({
    url: '/v1/user/uninstallLogo/'+ fileName,
    method: 'delete',
  })
}

// 获取用户详细信息
export function getInfo(userReq) {
  return request({
    url: '/v1/user/data/getSingleUserInfo',
    method: 'post',
    data: userReq
  })
}

// 找回用户密码
export function findPassword(findPasswordReq) {
  return request({
    url: '/v1/user/data/findPassword',
    method: 'post',
    data: findPasswordReq
  })
}

// 获取用户详细信息
export function getCertify(certifyReq) {
  return request({
    url: '/v1/user/data/getCertify',
    method: 'post',
    data: certifyReq
  })
}


// 注销用户
export function destroyUser(destroyUserReq) {
  return request({
    url: '/v1/system/destroyUser',
    method: 'post',
    data: destroyUserReq
  })
}





