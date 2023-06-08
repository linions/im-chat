import request from '../../utils/request'

// (聊天会话)
export function ConversationList(getConversationReq) {
  return request({
    url: '/v1/conversation/getConversationList',
    method: 'post',
    data: getConversationReq
  })
}


// 获取或者创建会话
export function getConversation(getConversationReq) {
  return request({
    url: '/v1/conversation/getConversation',
    method: 'post',
    data: getConversationReq
  })
}


// 删除会话
export function deleteConversation(deleteConversationReq) {
  return request({
    url: '/v1/conversation/deleteConversation',
    method: 'post',
    data: deleteConversationReq
  })
}

// 置顶会话
export function topConversation(topConversationReq) {
  return request({
    url: '/v1/conversation/updateConversation',
    method: 'put',
    data: topConversationReq
  })
}
