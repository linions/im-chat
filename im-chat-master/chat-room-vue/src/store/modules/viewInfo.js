import { friendsList } from '../../api/friends/friend'
import { groupList } from '../../api/group/group'
import { ConversationList } from '../../api/conversation/conversation'
import {defineStore} from 'pinia'

import { encrypt } from '../../utils/jsencrypt'
const useViewStore = defineStore('viewInfo', {
  state: () => ({
    conversationList:[],
    friendList:[],
    groupList: [],
    isFriendShipRead: true,
    isGroupRead: true,
    isNotifyRead: true,
  }),
  actions: {
     async getConversationList(v){
            return new Promise((resolve, reject) => {
              ConversationList(v)
                .then((res) => {
                  if(res.code == 200){
                    this.conversationList = res.data
                    resolve(res.data)
                  }
                  else{
                    console.log('get error ', res)
                  }
                })
                .catch((err) => {
                  console.log('get error ', err)
                  reject('获取最近聊天记录信息失败')
                })
              })
        },
    getFriendList(allFriendShipReq){
      return new Promise((resolve, reject) => {
        friendsList(allFriendShipReq)
        .then((res) => {
          if(res.code == 200){
            this.friendList = res.data
            resolve(res.data)
          }
          else{
            console.log('get error ', res)
          }
        })
        .catch((err) => {
          console.log('get error ', err)
          reject('获取好友列表失败')
        })
      })
  
    },

    getGroupList(getGroupReq){
        return new Promise((resolve, reject) => {
        groupList(getGroupReq)
          .then((res) => {
            if(res.code == 200){
              this.groupList = res.data.groupList
              resolve(res.data)
            }
            else{
              console.log('get error ', res)
            }
          })
          .catch((err) => {
            console.log('get error ', err)
            reject('获取组群列表失败')
          })
        })
    
      }
  }
})
export default useViewStore
