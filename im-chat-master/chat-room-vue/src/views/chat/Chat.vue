<template>
    <ChatBackground></ChatBackground>
    <div class="box">
        <div style="display: flex; height: 100%; width: 100%;">
            <ChatLeftNav></ChatLeftNav>
            <ChatMiddleNav></ChatMiddleNav>
            <div class="chatContent">
                <PersonalCenter v-if="switchStore.isPersonal"></PersonalCenter>
                <FriendCenter v-else-if="switchStore.isFriendIntro"></FriendCenter>
                <GroupCenter v-else-if="switchStore.isGroupIntro"></GroupCenter>
                <GroupRequest v-else-if="switchStore.isGroups"></GroupRequest>
                <FriendRequest v-else-if="switchStore.isFriends"></FriendRequest>
                <VideoChat v-else-if="switchStore.isVideo"></VideoChat>
                <Notification v-else-if="switchStore.isNotification"></Notification>
                <ChatContent v-else></ChatContent>
            </div>
        </div>
    </div>
</template>

<script setup>
import ChatBackground from '../../components/Chat/chatBackground.vue'
import ChatLeftNav from '../../components/Chat/chatLeftNav.vue'
import ChatMiddleNav from '../../components/Chat/chatMiddleNav.vue'
import ChatContent from '../../components/Chat/chatContent.vue'
import PersonalCenter from '../../components/Chat/Personal/personalCenter.vue'
import FriendCenter from '../../components/Chat/Personal/friendCenter.vue'
import GroupCenter from '../../components/Chat/Personal/groupCenter.vue'
import FriendRequest from '../../components/Chat/Friend/FriendRequest.vue'
import GroupRequest from '../../components/Chat/Group/GroupRequest.vue'
import Notification from '../../components/Chat/notification.vue'
import VideoChat from '../../components/Chat/videoChat.vue'
import useUserStore from '../../store/modules/user'
import useChatStore from '../../store/modules/chat'
import useSwitchStore from '../../store/modules/switch'
import { watch, ref, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import moadl from '../../plugins/modal'
import {imClient} from '../../lim/core/ImClient'
import {getInfo} from '../../api/user/user'
import {getRelation} from '../../api/friends/friend'
import useViewStore from '../../store/modules/viewInfo'
import { ElMessage, ElMessageBox,ElNotification} from "element-plus";

const userStore = useUserStore()
const chatStore = useChatStore()
const switchStore = useSwitchStore()
const viewStore = useViewStore()

// const ListenerMap = listenerStore()

let userInfo = sessionStorage.getItem('userInfo');
const im = sessionStorage.getItem('imClient');
let socket = JSON.parse(im);
console.log("socket",socket)
console.log("chatStore.chatSocket",chatStore.chatSocket)
let info = JSON.parse(userInfo);
console.log("info",info)

const router = useRouter()


const allFriendShipReq = ref({
    fromId: info.userId,
    appId: info.appId
})

const getChatReq = ref({
    fromId: info.userId,
    appId: info.appId
})
const getGroupReq = ref({
    memberId: info.userId,
    appId: info.appId,
    operator:info.userId,
})

const getConversationReq = ref({
    fromId: info.userId,
    toId : "",
    groupId : "",
    appId: info.appId,
    conversationType: 1
})
const ListenerMap = {
	    onSocketConnectEvent: (option, status, data) => {
	        console.log("已建立连接:" + JSON.stringify(status));
	    },
	    onSocketErrorEvent: (error) => {
	        console.log("连接出现错误:" + error);
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
            e = JSON.parse(e)
            if(e.command == 2010){
                if(e.data.mute == 0){
                    ElNotification({
                        title: "禁言解除",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>群编号：</span>" + e.data.groupId,
                        type: 'warning',
                    })
                    if(e.data.memberId == userStore.userInfo.userId){
                        moadl.msgWarning("您的禁言解除")
                    }
                }
                if(e.data.mute == 1){
                    ElNotification({
                        title: "群禁言",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>群编号：</span>" + e.data.groupId + "<br><span style= 'color: #8a98c9'>成员编号：</span>" + e.data.memberId,
                        type: 'warning',
                    })
                    if(e.data.memberId == userStore.userInfo.userId){
                        moadl.msgWarning("您被禁言")
                    }
                }
                
                // chatStore.chatGroup = chatStore.chatGroup
            }
            if(e.command == 1046){
                // 单聊发送消息失败
                if(!e.data.ok){
                    chatStore.onlineMsg[chatStore.onlineMsg.length - 1].error = true
                    ElMessage.error(e.data.msg)
                }
            }
            
            // 群聊发送消息失败
            if(e.command == 2047) {
                if(!e.data.ok){
                    chatStore.onlineMsg[chatStore.onlineMsg.length - 1].error = true
                    ElMessage.error(e.data.msg)
                }
            }
            // 好友上下线通知
            if(e.command == 4004) {
                viewStore.getFriendList(allFriendShipReq.value)
                viewStore.getConversationList(getChatReq.value)
            }
            if(e.command == 4007){
                var title = null
                if(e.data.type == 1){
                    ElNotification({
                        title: "系统公告",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>主题：</span> "+ e.data.title +"<br><span style= 'color: #8a98c9'>内容：</span>" + e.data.content,
                        type: 'info',
                    })
                }
                if(e.data.type == 2){
                    ElNotification({
                        title: "用户反馈",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>主题：</span> "+ e.data.title +"<br><span style= 'color: #8a98c9'>内容：</span>" + e.data.content + "<br><span style= 'color: #8a98c9'>反馈：</span>" + e.data.feedBack,
                        type: 'info',
                    })
                }
                if(e.data.type == 3){
                    ElNotification({
                        title: "系统通知",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>主题：</span> "+ e.data.title +"<br><span style= 'color: #8a98c9'>内容：</span>" + e.data.content,
                        type: 'info',
                    })
                }
                 viewStore.isNotifyRead = false
               
            }
            if(e.command == 4008){
                if(e.data.type == 2){
                    ElNotification({
                        title: e.data.title,
                        dangerouslyUseHTMLString: true,
                        message: "您已收到用户举报！<br><span style= 'color: #8a98c9'>内容：</span>" + e.data.content,
                        type: 'error',
                    })
                }
                viewStore.isNotifyRead = false
            }
            //好友申请
            if(e.command == 3003){
                ElNotification({
                    title: "您有新的好友申请",
                    dangerouslyUseHTMLString: true,
                    message: "<span style= 'color: #8a98c9'>来自：</span> "+ e.data.fromId +"<br><span style= 'color: #8a98c9'>来源：</span>" + e.data.addSource,
                    type: 'info',
                })
                viewStore.isFriendShipRead = false
            }
            //添加/删除好友
            if(e.command == 3000 || e.command == 3002){
                console.log(allFriendShipReq.value)
                viewStore.getFriendList(allFriendShipReq.value)
            }
            //踢出群聊好友
            if(e.command == 2006 || e.command == 2002){
                viewStore.getGroupList(getGroupReq.value);
                if(e.command == 2002){
                    ElNotification({
                        title: "入群通知",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>群编号：</span>" + e.data.groupId + "<br><span style= 'color: #8a98c9'>群昵称：</span>" + e.data.groupName,
                        type: 'warning',
                    })
                }
                if(e.command == 2006){
                    ElNotification({
                        title: "您被踢出群聊",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>群编号：</span>" + e.data.groupId + "<br><span style= 'color: #8a98c9'>群昵称：</span>" + e.data.groupName,
                        type: 'warning',
                    })
                }
            }
            //群解散
            if(e.command == 2007){
                viewStore.getGroupList(getGroupReq.value);
                ElNotification({
                    title: "群解散",
                    dangerouslyUseHTMLString: true,
                    message: "<span style= 'color: #8a98c9'>群编号：</span>" + e.data.groupId ,
                    type: 'warning',
                })
            }
            
            //申请入群
            if(e.command == 2001){
                if(e.data.status != null){
                    let content = "拒绝"
                    if(e.data.status == 1){
                        content = "通过"
                    }
                    ElNotification({
                        title: "入群申请",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>状态：</span> " +  content,
                        type: 'info',
                    })
                    viewStore.getGroupList(getGroupReq.value);
                }else{
                    ElNotification({
                        title: "有新的成员入群申请",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>群号：</span> "+ e.data.groupId,
                        type: 'info',
                    })
                    viewStore.isGroupRead = false

                }
            }
            
	    },
	    onP2PMessage: (e) => {
			console.log("onP2PMessage ：" + e );
			e = JSON.parse(e)
            if(switchStore.isChat == true && chatStore.chatUser != null && e.data.fromId == chatStore.chatUser.userId){
                let msg = {}
                msg.fromId =  e.data.fromId,
                msg.messageTime = getDate(),
                msg.appId = e.data.appId,
                msg.toId =  e.data.toId
                msg.type = e.data.type
                if(e.data.type == 1){
                    msg.messageBody =  e.data.messageBody
                }else{
                    e.data.fileContent.size = bytesToSize(Number.parseInt(e.data.fileContent.size))
                    msg.messageBody = e.data.fileContent
                }
                // chatStore.chatLog = {...msg}
                chatStore.onlineMsg.push({...msg})
            }else{
                viewStore.getConversationList(getChatReq.value)
            }
	    },
        onGroupMessage: (e) => {
			console.log("onGroupMessage ：" + e );
			e = JSON.parse(e)
            if(switchStore.isGroupChat == true && chatStore.chatGroup != null && e.data.groupId == chatStore.chatGroup.groupId){
                let msg = {}
                msg.fromId =  e.data.fromId,
                msg.messageTime = getDate(),
                msg.appId = e.data.appId,
                msg.groupId =  e.data.groupId
                msg.type = e.data.type
                if(e.data.type == 1){
                    msg.messageBody =  e.data.messageBody
                }else{
                    e.data.fileContent.size = bytesToSize(Number.parseInt(e.data.fileContent.size))
                    msg.messageBody = e.data.fileContent
                }
                var getInfoReq ={}
                getInfoReq.appId = msg.appId
                getInfoReq.userId = msg.fromId
                getInfo(getInfoReq).then((res)=>{
                    if(res.code == 200){
                        msg.userData = res.data
                        chatStore.onlineMsg.push({...msg})
                        console.log("chatStore.onlineMsg = ",chatStore.onlineMsg)
                    }
                })
                
            }
	    },
		onLogin: (uid) => {
		    console.log("用户"+uid+"登陆sdk成功" );
		},
        onVoiceCall: (e) => {
            console.log("onVideoCall e：" + e );
			e = JSON.parse(e)
             // 收到请求
             if(e.data.messageKey != null){
                var userReq = {}
                userReq.userId = e.data.fromId
                userReq.appId = e.appId
                getUserInfo(userReq)
                var getRelationReq = {}
                getRelationReq.fromId = userStore.userInfo.userId
                getRelationReq.toId = e.data.fromId
                getRelationReq.appId = e.appId
                getRelationInfo(getRelationReq)
                switchStore.isVoice = true;
                chatStore.state = 2
                chatStore.messageKey = e.data.messageKey
            }
        },
        onVideoCall: (e) => {
            console.log("onVideoCall e：" + e );
			e = JSON.parse(e)
            // 收到请求
            if(e.data.messageKey != null){
                var userReq = {}
                userReq.userId = e.data.fromId
                userReq.appId = e.appId
                getUserInfo(userReq)
                var getRelationReq = {}
                getRelationReq.fromId = userStore.userInfo.userId
                getRelationReq.toId = e.data.fromId
                getRelationReq.appId = e.appId
                getRelationInfo(getRelationReq)
                switchStore.isVideo = true;
                chatStore.state = 2
                chatStore.messageKey = e.data.messageKey
            }
        },
        acceptCall: (e) => {
            console.log("acceptCall e：" );
            e = JSON.parse(e)
            ElMessage.success("对方已接受,开启聊天！")
            chatStore.messageKey = e.data.messageKey
            chatStore.state = 3
        }, 
        sendOffer: (e) => {
            console.log("sendOffer e：");
            e = JSON.parse(e)
            chatStore.sdp = JSON.parse(e.data.mediaContent.sdp)
            chatStore.candidate = JSON.parse(e.data.mediaContent.candidate)
        },
        sendAnswer: (e) => {
            console.log("sendAnswer e：");
            e = JSON.parse(e)    
            chatStore.sdp = JSON.parse(e.data.mediaContent.sdp)
            chatStore.candidate = JSON.parse(e.data.mediaContent.candidate)
            chatStore.isReceive = true
        }, 
        videoAck: (e) => {
            console.log("videoAck e：",e);
            e = JSON.parse(e)

            if( e.data.serverSend == true ){
                if(e.data.type == 2 || e.data.type == 1){
                    chatStore.state = 1
                    chatStore.messageKey = e.data.messageKey
                    console.log("chatStore.messageKey ：" + chatStore.messageKey );
                }
                if(e.data.type == 3){
                    chatStore.state = 3
                }
            }else{
                if(e.data.type == 2){
                    // 音视频通话对方不在线
                    ElMessage.error("对方不在线,无法进行视频通话")
                    switchStore.isVideo = false;
                }
                if(e.data.type == 1){
                    // 音视频通话对方不在线
                    ElMessage.error("对方不在线,无法进行语音通话")
                    switchStore.isVoice = false;
                }
                
            }
        },
        rejectCall: (e) => {
            console.log("rejectCall e：");
            e = JSON.parse(e)
            ElMessage.warning("对方已拒绝！")
            // switchStore.isVideo = false;
            chatStore.state = 0;
            chatStore.messageKey = null
            chatStore.isReceive = false
            switchStore.isVoice = false;
        },
        hungUpCall: (e) => {
            console.log("hungUpCall e：");
            e = JSON.parse(e)
            ElMessage.warning("对方已挂断！")
            // switchStore.isVideo = false;
            chatStore.state = 0;
            chatStore.messageKey = null
            chatStore.isReceive = false
            switchStore.isVoice = false;
        },
        timeOutCall: (e) => {
            console.log("timeOutCall e：" );
            e = JSON.parse(e)
        },
        cancelCall: (e) => {
            console.log("cancelCall e：");
            e = JSON.parse(e)

            ElMessage.warning("对方已取消！")
            // switchStore.isVideo = false;
            switchStore.isVoice = false;
            chatStore.state = 0;
            chatStore.messageKey = null
            chatStore.isReceive = false 
        },	
        userOnline: (e) => {
            console.log("userOnline e：" + e );
            e = JSON.parse(e)
        },	
            
	};


// 刷新isChat
async function setUp(){
    switchStore.isChat = false
    let getListData = ref({
        fromId: userStore.userInfo.userId,
        toId: chatStore.chatUser.userId,
        ownerId: userStore.userInfo.userId,
        appId:userStore.userInfo.appId
    })
    nextTick(() => {
        chatFriendLogList(getListData.value).then((res) => {
            if (res.code == 200) {
                chatStore.chatLogList = res.data
                chatStore.onlineMsg = []
                switchStore.isChat = true
            }
        })
    })
}


// 获取文件大小
function bytesToSize(bytes) {
    if (bytes === 0) return '0 B';
    var k = 1024
    var sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
    var i = Math.floor(Math.log(bytes) / Math.log(k));
   return (bytes / Math.pow(k, i)).toPrecision(3) + '' + sizes[i];
}

async function getUserInfo(userReq){
    getInfo(userReq).then((res) => {
        if (res.code == 200) {
            chatStore.chatUser = res.data
            if(chatStore.state != 2){{
                chatStore.state = 2
            }}
            console.log("chatStore.chatUser = ",chatStore.chatUser)
        }
    })
}

async function getRelationInfo(getRelationReq){
    getRelation(getRelationReq).then((res) => {
        if (res.code == 200) {
            chatStore.friendShip = res.data
            
            if(chatStore.state != 2){{
                chatStore.state = 2
            }}
            console.log("chatStore.friendShip = ",chatStore.friendShip)
        }
    })
}

// 获取日期
function getDate() {
    let d = new Date();
    // 获取年
    let year = d.getFullYear();
    // 获取月
    let month = d.getMonth() + 1;
    // 获取日
    let date = d.getDate();
    // 获取时
    let hour = d.getHours();
    // 获取分
    let minutes = d.getMinutes();
    // 获取秒
    let seconds = d.getSeconds();
    // getMilliseconds() 获取毫秒数
    // getTime() 从1970年1月1日0点0时0分到当前时间的毫秒数
    return year + "-" + month + "-" + date + " " + hour + ":" + minutes + ":" + seconds
}

watch(async (v) => {
    if (sessionStorage.getItem('userInfo') == null) {
        moadl.msgError("请登录！")
        router.push({
            path: "/login"
        })
        setTimeout(() => {}, 500);
        return
    }
     //重新连接   
    var listeners = {};
    for (const v in ListenerMap) {
        listeners[v] = ListenerMap[v];
    }
    // if(chatStore.chatSocket == null){
        imClient.init(socket.ip,socket.port,info.appId,info.userId,socket.clientType,socket.userSign,listeners,function (imClient) {
            chatStore.chatSocket = imClient
            console.log("chatStore.chatSocket = ",chatStore.chatSocket)
            sessionStorage.setItem("imClient",JSON.stringify(imClient))
        });
        await userStore.getUserInfo(userInfo);
    // }
    

});


userStore.userInfo = info
// console.log("chatStore.chatSocket = ",chatStore.chatSocket)

</script>


<style scoped>
.box {
    width: 1100px;
    height: 700px;
    background-color: #f5f5f5;
    border-radius: 10px;
}

.chatContent {
    width: 63%;
    height: 100%;
    /* background-color: red; */
    border-left: rgba(121, 163, 159, 0.3) 1px solid;
    overflow: hidden;
}
</style>