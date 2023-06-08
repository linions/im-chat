import request from '../utils/request';

export const fetchData = query => {
    return request({
        url: './table.json',
        method: 'post',
        params: query
    });
};

/*
* admin 登陆  POST
* /adminLogin
* 邮箱 密码
* */

export const adminLogin = data => {
    return request({
        url: '/v1/user/adminLogin',
        method: 'post',
        data
    });
};

export function adminLogout(userId){
    return request({
        url: '/v1/user/adminLogout/' + userId,
        method: 'post',
    });
};

// 获取用户详细信息
export function getInfo(userReq) {
    return request({
      url: '/v1/user/data/getSingleUserInfo',
      method: 'post',
      data: userReq
    })
  }


/*
* 获取用户数量 聊天数量 总共建立好友量 post
* userNumAndchatNum
* 携带token
* */

export const getSysInfo = (sysDataReq) => {
    return request({
        url: '/v1/system/getSysData',
        method: 'post',
        headers:{
            crazytoken:localStorage.getItem('crazyAdminToken')
        },
        data: sysDataReq
    });
};


// 获取用户类型百分比
export const getUserPercentage = (appId) => {
    return request({
        url: '/v1/user/getUserPercentage/' + appId,
        method: 'post',
    });
};

// 获取用户折线图百分比
export const getDataOfWeek = (appId) => {
    return request({
        url: '/v1/user/getDataOfWeek/' + appId,
        method: 'get',
    });
};
/*
* 获取用户信息
* adminfindAlluser
* 携带token
* */
export const getUserByPage = (userReq) => {
    return request({
        url: '/v1/user/getUserByPage',
        method: 'post',
        headers:{
            crazytoken:localStorage.getItem('crazyAdminToken')
        },
        data: userReq
    });
};

// 注销用户
export const destroyUser = (userId,operator) => {
    return request({
        url: '/v1/system/destroyUser/' + userId + "/" + operator,
        method: 'post',
        headers:{
            crazytoken:localStorage.getItem('crazyAdminToken')
        },
    });
};

// 禁用用户
export const changeStatus = (userId,status,operator) => {
    return request({
        url: '/v1/user/changeStatus/' + userId + '/' + status + "/" + operator,
        method: 'put',
        headers:{
            crazytoken:localStorage.getItem('crazyAdminToken')
        },
    });
};

// 创建账号
export const createUser = (user) => {
    return request({
        url: '/v1/user/data/create',
        method: 'post',
        headers:{
            crazytoken:localStorage.getItem('crazyAdminToken')
        },
        data:user
    });
};

// 获取操作日志信息
export const getLog = (logReq) => {
    return request({
        url: '/v1/system/getLog' ,
        method: 'post',
        data: logReq
    });
};


// 删除操作日志信息
export const deleteLog = (sysLog,operator) => {
    return request({
        url: '/v1/system/deleteLog/' +  operator  ,
        method: 'delete',
        data: sysLog,
    });
};

/*
* 修改用户信息 POST
* adminChangeUser
* crazyId changeData:{username,__V,viaUrl,tel}
* */
export const changeUser = (changeData) => {
    return request({
        url: '/v1/user/data/modifyUserInfo',
        method: 'put',
        data:changeData,
        headers:{
            crazytoken:localStorage.getItem('crazyAdminToken')
        }
    });
};



