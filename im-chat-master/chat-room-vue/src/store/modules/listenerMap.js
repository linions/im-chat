import { defineStore,storeToRefs} from 'pinia'

const listenerStore = defineStore('listener', {
    state: () => {
        return {
          ListenerMap : {
            onSocketConnectEvent: (option, status, data) => {
                console.log("已建立连接:" + JSON.stringify(status));
            },
            onSocketErrorEvent: (error) => {
                console.log("连接出现错误:" + userId);
            },
            onSocketReConnectEvent: () => {
                console.log("正在重连:" );
            },
            onSocketCloseEvent: () => {
                console.log("连接关闭:" );
            },onSocketReConnectSuccessEvent: () => {
              console.log("重连成功" );
          },
            onTestMessage: (e) => {
                console.log("onTestMessage ：" + e );
            },
            onP2PMessage: (e) => {
            console.log("onP2PMessage ：" + e );
            e = JSON.parse(e)
            console.log('P2PMessage', e.data);
            },
          onLogin: (uid) => {
              console.log("用户"+uid+"登陆sdk成功" );
          }		
        }
      
        }
    },
    actions: {
        
    },
    getters: {

    }
})

export default listenerStore;