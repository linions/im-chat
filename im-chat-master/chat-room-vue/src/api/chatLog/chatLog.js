import request from '../../utils/request'

// 下载文件
export function downLoadFile(fileReq) {
  return request({
    url: '/v1/message/downLoadFile',
    method: 'post',
    data: fileReq
  })
}

// 聊天好友记录列表
export function chatFriendLogList(getFriendMsgReq) {
  return request({
    url: '/v1/message/getChatMessage',
    method: 'post',
    data: getFriendMsgReq
  })
}

// 聊天群聊记录列表
export function chatGroupLogList(getGroupMsgReq) {
  return request({
    url: '/v1/group/getGroupChatMessage',
    method: 'post',
    data: getGroupMsgReq
  })
}

// 聊天好友历史记录列表
export function chatFriendLogHistoryList(data) {
  return request({
    url: '/business/ChatLog/chatFriendLogHistoryList',
    method: 'post',
    data: data
  })
}

// 聊天群聊历史记录列表
export function chatGroupLogHistoryList(data) {
  return request({
    url: '/business/ChatLog/chatGroupLogHistoryList',
    method: 'post',
    data: data
  })
}

// 获取自己发的消息(最新)
export function slefNewMsg(data) {
  return request({
    url: '/business/ChatLog/slefNewMsg',
    method: 'post',
    data: data
  })
}

// 已读处理  (聊天消息已读)
export function read(data) {
  return request({
    url: '/business/ChatLog/read',
    method: 'post',
    data: data
  })
}

// 群聊已读处理
export function groupRead(data) {
  return request({
    url: '/business/ChatLog/groupRead',
    method: 'post',
    data: data
  })
}

// 添加好友聊天记录
export function addChatFriendLog(data) {
  return request({
    url: '/v1/message/send',
    method: 'post',
    data: data
  })
}

// 创建群聊
export function createGroup(data) {
  return request({
    url: '/business/ChatLog/CreateGroup',
    method: 'post',
    data: data
  })
}
