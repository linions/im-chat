import request from '../utils/request'

// 获取验证码
export function getCode(getCodeReq) {
  return request({
    url: '/v1/user/getCode',
    method: 'post',
    data: getCodeReq,
  })
}

// 登录方法
export function login(loginReq) {
  return request({
    url: '/v1/user/login',
    method: 'POST',
    data: loginReq,
  })
}

// 修改用户信息
export function updateChatUser(userInfoReq) {
  return request({
    url: '/v1/user/data/modifyUserInfo',
    method: 'put',
    data: userInfoReq
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

// 退出方法
export function logout(logOutReq) {
  return request({
    url: '/v1/user/logOut',
    method: 'post',
    data: logOutReq
  })
}


/**
 * 查看好友状态
 */
export function checkFriendStatus(logoutReq) {
  return request({
    url: '/v1/user/data/logout',
    method: 'post',
    data: logoutReq
  })
}