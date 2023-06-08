import { defineStore,storeToRefs} from 'pinia'
import useSwitchStore from './switch'
import {chatFriendLogList,chatGroupLogList } from '../../api/chatLog/chatLog'
import { ref } from 'vue'

const useChatStore = defineStore('chat', {
    state: () => {
        return {
            chatSocket:null,
            chatUser: null,
            chatGroup: null,
            friendShip: null,
            chatLog: {},
            groupChatLog: {},
            groupChatLogList: [],
            chatLogList: [],
            onlineMsg: [],
            state:0,
            messageKey:null,
            sdp:null,
            candidate: null,
            isReceive:false,
            remoteVideo:ref(),
            localVideo:ref(),
            isOffer:false,
            isAnswer:false,
            isVoice:false,
        }
    },
    actions: {
        async getFriendChatList(v){
          return new Promise((resolve, reject) => {
            chatFriendLogList(v)
              .then((res) => {
                if(res.code == 200){
                  this.chatLogList = res.data
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
      async getGroupChatList(v){
        return new Promise((resolve, reject) => {
          chatGroupLogList(v)
            .then((res) => {
              if(res.code == 200){
                this.groupChatLogList = res.data
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
    }
    },
    getters: {

    }
})

export default useChatStore;