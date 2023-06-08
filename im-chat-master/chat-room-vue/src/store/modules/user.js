import { login, logout, getInfo } from '../../api/login'
import { setToken, removeToken } from '../../utils/auth'
import defAva from '../../assets/img/profile.jpg'
import img from '../../assets/img/profile.jpg'
import Cookies from 'js-cookie'
import {defineStore} from 'pinia'

import { encrypt } from '../../utils/jsencrypt'
const useUserStore = defineStore('user', {
  state: () => ({
    name: '',
    userInfo:{},
    status: 0
  }),
  actions: {
    getUserInfo(userReq){
      return new Promise((resolve, reject) => {
        getInfo(userReq)
        .then((res) => {
          if(res.code == 200){
            this.status = 1
            if (res.data.photo == null) {
              res.data.photo = img
            }
            this.userInfo = res.data
            sessionStorage.setItem('userInfo', JSON.stringify(res.data));
            Cookies.set('userId', this.userInfo.userId, { expires: 30 })
            resolve(res.data)
          }
          else{
            console.log('get error ', res)
          }
        })
        .catch((err) => {
          console.log('get error ', err)
          reject('获取用户信息失败')
        })
      })
  
    }
  }
})
export default useUserStore
