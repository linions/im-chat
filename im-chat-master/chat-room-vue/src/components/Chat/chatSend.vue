<template>
    <div>
        <div class="toolbar" v-if="switchStore.isChat || switchStore.isGroupChat">
            <div class="tools">
                <!-- <UploadImage2 v-model="asda" ref="uploadRef" :drag="true" /> -->
                <V3Emoji style="padding-bottom: 2.5px" size="mid" :recent="true" fix-pos="upright"
                    :options-name="optionsName" :custom-size="customSize" @click-emoji="appendText" class="history">
                    <img src="../../assets/img/emoji.png" alt="">
                </V3Emoji>

                <el-upload  class="avatar-uploader" :action="uploadImgUrl" :show-file-list="false"
                    :on-success="handleImgSuccess" :before-upload="beforeUploadImg" name="file" accept=".png, .jpg, .jpeg" >
                    <img src="../../assets/img/pic.png" alt="">
                </el-upload>

                <el-upload  :show-file-list="false" :action="uploadImgUrl" :before-upload="beforeUploadFile" name="file" 
                    accept=".pdf, .doc, .docx, .xls, .xlsx,.txt" :on-success="handleFileSuccess">
                    <img src="../../assets/img/file.png" alt="">
                </el-upload>

                <div  class="history">
                    <img src="../../assets/img/talk.png" alt="">
                </div>
            </div>
            <div class="tools">
                <div @click="handleChatHistoryLog" class="history">
                    <img src="../../assets/img/history.png" alt="">
                </div>
                <div v-if="switchStore.isGroupChat"  class="history" @click="showGroupIntro()">
                    <img src="../../assets/img/detail.png" alt="">
                </div>
            </div>
            
        </div>
        <div class="send_box">
            <!-- onkeydown="if (event.keyCode === 13) event.preventDefault();" -->

            <div ref="inputRef" v-if="switchStore.isChat" class="textArea" maxlength="800" autofocus
                @keydown="handleEntry($event)" contenteditable="true">
            </div>

            <div ref="inputRef" v-if="switchStore.isGroupChat" class="textArea" maxlength="800" autofocus
                @keydown="handleGroupEntry($event)" contenteditable="true">
            </div>


            <textarea v-if="!switchStore.isChat && !switchStore.isGroupChat" class="textArea" maxlength="800" autofocus disabled>
            </textarea>
        </div>
        <div class="btn_box">
            <button v-if="switchStore.isChat || switchStore.isGroupChat" class="close" @click="handleClose">关闭(c)</button>
            <button v-if="switchStore.isChat" :disabled="chatStore.friendShip.status != 1" @click="handelSend">发送(Enter)</button>
            <button v-if="switchStore.isGroupChat" :disabled="memberInfo == null || memberInfo.mute == 1" @click="handelGroupSend()">发送(Enter)</button>
        </div>
    </div>

    <el-dialog v-model="isAdd" title="好友历史聊天记录" width="30%" @close="closeFriendLog()" :modal="false">
        <div class="messageType" style="margin-bottom: 10px">
            <el-button :disabled="getFriendMsgReq.type == 1" type="primary" @click="getTextMsg()" plain>文本</el-button>
            <el-button :disabled="getFriendMsgReq.type == 2" type="success" @click="getImgMsg()" plain>图片</el-button>
            <el-button :disabled="getFriendMsgReq.type == 3" type="info" @click="getFileMsg()" plain>文件</el-button>
        </div>
        <el-input v-model="logQueryData" placeholder="搜索"></el-input>

        <div class="historyBox">
            <div class="_message" v-for="(item,index) in msglogList" :key="index" style="font-size: 15px;">
                <div class="imgbox"  v-if="item.fromId == chatStore.chatUser.userId && item.toId == userStore.userInfo.userId">
                    <img :src=chatStore.chatUser.photo alt="头像">
                </div>
                <div class="imgbox"  v-if="item.fromId == userStore.userInfo.userId && item.toId == chatStore.chatUser.userId">
                    <img :src=userStore.userInfo.photo alt="头像">
                </div>

                <div class="_message_content_box" >
                    <div class="_message_name" v-if="item.fromId == chatStore.chatUser.userId && item.toId == userStore.userInfo.userId">
                            {{chatStore.friendShip.remark == null || chatStore.friendShip.remark == '' ?   chatStore.chatUser.nickName : chatStore.friendShip.remark}}
                    </div>
                    <div class="_message_name"  v-if="item.fromId == userStore.userInfo.userId && item.toId == chatStore.chatUser.userId">
                            {{userStore.userInfo.nickName}}
                    </div>
                    <div v-if="item.type == 1" class="_message_content" v-html="item.messageBody"></div>
                    <div  class="_message_content" v-else-if="item.type == 2">
                        <img style="width: 60px!important;height: 60px;" :src=item.messageBody alt="">
                    </div>
                    <div v-else-if="item.type == 3" class="_message_content" style="height: 40px;">
                        <img style=" width: 30px!important;height: 30px;" src='../../assets/img/file.png' alt="">
                        <span style="margin-left: 10px; color:black"> {{ item.fileDto.name }}</span>
                        <span style="margin-left: 10px;">{{ item.fileDto.size }}</span>
                    </div>
                </div>

                <div class="_message_time">
                    <div>{{item.messageTime}}</div>
                </div>
            </div>

        </div>
    </el-dialog>

    <el-dialog v-model="isGroupHistory" title="群聊历史聊天记录" width="30%" @close="isGroupHistory = false" :modal="false">
        <el-input v-model="logQueryData" placeholder="搜索"></el-input>
        <div class="messageType" style="margin-bottom: 10px">
            <el-button :disabled="getFriendMsgReq.type == 1" type="primary" @click="getTextMsg()" plain>文本</el-button>
            <el-button :disabled="getFriendMsgReq.type == 2" type="success" @click="getImgMsg()" plain>图片</el-button>
            <el-button :disabled="getFriendMsgReq.type == 3" type="info" @click="getFileMsg()" plain>文件</el-button>
        </div>
        <div class="historyBox">
            <div class="_message" v-for="(item,index) in msglogList" :key="index">
                <div class="imgbox"  v-if="item.fromId == userStore.userInfo.userId">
                    <img :src=userStore.userInfo.photo alt="头像">
                </div>
                <div class="imgbox"  v-else>
                    <img :src=item.userData.photo alt="头像">
                </div>
                <div class="_message_content_box" >
                    <div class="_message_name" v-if="item.fromId == userStore.userInfo.userId">
                            {{userStore.userInfo.nickName}}
                    </div>
                    <div class="_message_name"  v-else>
                            {{item.userData.nickName }}
                    </div>
                    <div v-if="item.type == 1" class="_message_content" v-html="item.messageBody"></div>
                    <div  class="_message_content" v-else-if="item.type == 2">
                        <img style="width: 60px!important;height: 60px;" :src=item.messageBody alt="">
                    </div>
                    <div v-else-if="item.type == 3" class="_message_content" style="height: 40px;">
                        <img style=" width: 30px!important;height: 30px;" src='../../assets/img/file.png' alt="">
                        <span style="margin-left: 10px; color:black"> {{ item.fileDto.name }}</span>
                        <span style="margin-left: 10px;">{{ item.fileDto.size }}</span>
                    </div>
                </div>

                <div class="_message_time">
                    <div>{{item.messageTime}}</div>
                </div>
            </div>

        </div>
    </el-dialog>


</template>

<script setup>
import { onMounted, ref, reactive, watch, computed } from 'vue'
import signalr from '../../utils/signalR'
import useUserStore from '../../store/modules/user'
import useSwitchStore from '../../store/modules/switch'
import useSocketChatStore from '../../store/modules/socketchat'
import useChatStore from "../../store/modules/chat";
import modal from '../../plugins/modal';
import { chatFriendLogList, chatGroupLogList } from '../../api/chatLog/chatLog'
import V3Emoji from 'vue3-emoji'
import 'vue3-emoji/dist/style.css'
import { groupInfo } from '../../api/group/group'
import {getInfo} from '../../api/user/user'
import {getMemberById} from '../../api/group/group'
import moadl from '../../plugins/modal'

// sdk.errorHandler()


const uploadImgUrl = ref("/v1/message/uploadMsgFile") // 上传的服务器地址

const userStore = useUserStore()
const switchStore = useSwitchStore()
const SocketChatStore = useSocketChatStore()
const chatStore = useChatStore()


let userInfo = userStore.userInfo

const msg = ref();
const inputRef = ref();
const titleName = ref();
let isAdd = ref(false)
let isGroupHistory = ref(false)
const isOnline = ref(false);
let type = 1
let messageBody = null
let isMore = ref(false);
let msglogList = ref([])
let logQueryData = ref(null)

let getFriendMsgReq = ref({
    fromId: userStore.userInfo.userId,
    ownerId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    search:null,
    type :1,
})

let getGroupMsgReq = ref({
    appId: null,
    groupId: null,
    search:null,
    type:1,
})

let groupInfoReq = ref({
    appId: userStore.userInfo.appId,
    operator: userStore.userInfo.userId
})

// 获取群聊信息
async function getGroupInfo(){
    await  groupInfo(groupInfoReq.value).then((res) => {
        if (res.code == 200) {
            switchStore.GroupIntroData = res.data
            console.log("GroupIntroData = " , switchStore.GroupIntroData)
             // console.log(v);
            switchStore.isGroupIntro = true
            switchStore.isFriendIntro = false
            switchStore.isPersonal = false
            switchStore.isFriends = false
            switchStore.isChat = false
            switchStore.isNotification = false
            switchStore.isGroupChat = false
        }
    })
}

// 简介
async function showGroupIntro() {
    groupInfoReq.value.groupId = chatStore.chatGroup.groupId
    getGroupInfo()
}

const memberInfo = ref()
watch(() => chatStore.chatGroup, async (value) => {
    console.log(value);
    //获取成员信息
    getMemberById(chatStore.chatGroup.groupId,chatStore.chatGroup.appId,userStore.userInfo.userId).then((res) => {
        if (res.code == 200) {
            memberInfo.value = res.data
        }
    })
})


const optionsName = {
    'Smileys & Emotion': '笑脸&表情',
    'Food & Drink': '食物&饮料',
    'Animals & Nature': '动物&自然',
    'Travel & Places': '旅行&地点',
    'People & Body': '人物&身体',
    Objects: '物品',
    Symbols: '符号',
    Flags: '旗帜',
    Activities: '活动'
};

const customSize = {
    //   'V3Emoji-width': '300px',
    'V3Emoji-height': '20rem',
    'V3Emoji-fontSize': '1rem',
    'V3Emoji-itemSize': '2rem'
};


const appendText = (val) => {
    // console.log(val);
    inputRef.value.innerHTML += val;
};

const queryParams = ref({
    ChatUserGuid: null
})
const queryGroupParams = ref({
    ChatGroupGuid: null
})

let addData = ref({
    appId: userStore.userInfo.appId,
    fromId: userStore.userInfo.userId,
    toId: null,
    messageBody: null,
    messageTime: null,
    error:false,
    type: null,
    size:null,
})

let addGroupData = ref({
    appId: userStore.userInfo.appId,
    fromId: userStore.userInfo.userId,
    groupId: null,
    messageBody: null,
    messageTime: null,
    error:false,
    type: null,
    size:null,
    userData:null,
})

// watch(() => chatStore.chatUser, async (value) => {
//     getListData.pageNum = 1
// })

// watch(() => chatStore.chatGroup, async (value) => {
//     getListData.pageNum = 1
// })

watch(() => logQueryData.value, async (value) => {
    console.log(value);
    getFriendMsgReq.value.search = value
    getGroupMsgReq.value.search = value
    console.log(getFriendMsgReq.value);
    if (switchStore.isChat) {
        getFriendList()
    }
    if (switchStore.isGroupChat) {
        getGroupList()
    }
})

async function handelSend() {
    isOnline.value = false

    if (!switchStore.isChat && !switchStore.isGroupChat) {
        modal.msgWarning("快去找好友聊天吧~")
        return
    }

    if (inputRef.value.innerHTML == false || inputRef.value.innerHTML == null) {
        modal.msgError('请输入内容');
        inputRef.value.innerHTML = null
        return
    }

    if(chatStore.friendShip.status != 1){
        moadl.msgError("非好友关系，无法发送消息")
        return
    }

        addData.value.toId = chatStore.chatUser.userId
        addData.value.messageTime = getDate()
        addData.value.type = type
        if(type == 1){
            addData.value.messageBody = inputRef.value.innerHTML
            chatStore.chatSocket.sendP2PMessage(addData.value.toId,addData.value.messageBody,addData.value.type)

        }else{
            addData.value.messageBody = messageBody
            chatStore.chatSocket.sendP2PFileMessage(addData.value.toId,addData.value.messageBody,addData.value.type)
        }
        // chatStore.chatLog = addData.value
        chatStore.onlineMsg.push({...addData.value})
        inputRef.value.innerHTML = null
        setTimeout(() => {
            switchStore.notOnlineChat = true
        }, 500);

    // }
    type = 1
    inputRef.value.innerHTML = null
    messageBody = null
}

// 群聊发送消息
async function handelGroupSend() {
    if (!switchStore.isChat && !switchStore.isGroupChat) {
        modal.msgWarning("快去找好友聊天吧~")
        return
    }

    if (inputRef.value.innerHTML == false || inputRef.value.innerHTML == null) {
        modal.msgError('请输入内容');
        inputRef.value.innerHTML = null
        return
    }

        addGroupData.value.groupId = chatStore.chatGroup.groupId
        addGroupData.value.messageTime = getDate()
        addGroupData.value.type = type
        if(type == 1){
            addGroupData.value.messageBody = inputRef.value.innerHTML
            chatStore.chatSocket.sendGroupMessage(addGroupData.value.groupId,addGroupData.value.messageBody,addGroupData.value.type)

        }else{
            addGroupData.value.messageBody = messageBody
            chatStore.chatSocket.sendGroupFileMessage(addGroupData.value.groupId,addGroupData.value.messageBody,addGroupData.value.type)
        }
        
        var getInfoReq ={}
        getInfoReq.appId = addGroupData.value.appId
        getInfoReq.userId = addGroupData.value.fromId
        getInfo(getInfoReq).then((res)=>{
            if(res.code == 200){
                addGroupData.value.userData = res.data
                chatStore.onlineMsg.push({...addGroupData.value})
                console.log("chatStore.onlineMsg = ",chatStore.onlineMsg)
            }
        })

        
        inputRef.value.innerHTML = null
        setTimeout(() => {
            switchStore.notOnlineChat = true
        }, 500);

    // }
        type = 1
        inputRef.value.innerHTML = null
        messageBody = null
}


function handleClose() {
    inputRef.value.innerHTML = null

    switchStore.isChat = false
    switchStore.isGroupChat = false
}

function closeFriendLog(){
    isAdd.value = false
    getFriendMsgReq.value.type = 1
    getFriendMsgReq.value.search = null
}

// 获取文件消息记录
function getFileMsg(){
    getGroupMsgReq.value.type = 3
    getFriendMsgReq.value.type = 3
    logQueryData.value = null
    if (switchStore.isChat) {
        isAdd.value = true
        getFriendList()
    }
    if (switchStore.isGroupChat) {
        isGroupHistory.value = true
        getGroupList()
    }
}

function getImgMsg(){
    getFriendMsgReq.value.type = 2
    getGroupMsgReq.value.type = 2
    logQueryData.value = null
    if (switchStore.isChat) {
        isAdd.value = true
        getFriendList()
    }
    if (switchStore.isGroupChat) {
        isGroupHistory.value = true
        getGroupList()
    }
}

// 获取文本消息记录
function getTextMsg(){
    getFriendMsgReq.value.type = 1
    getGroupMsgReq.value.type = 1
    logQueryData.value = null
    if (switchStore.isChat) {
        isAdd.value = true
        getFriendList()
    }
    if (switchStore.isGroupChat) {
        isGroupHistory.value = true
        getGroupList()
    }
}
// 获取图片消息记录
const handleImgSuccess = (value) => {
    if (value.code != 200) {
        modal.msgError(value.msg);
        return;
    }
    console.log(value.data);
    msg.value = value.data.url;
    inputRef.value.innerHTML += `<img style="width: 10rem!important;height: auto;" src=${msg.value} alt="">`
    type = 2
    messageBody = value.data
}
const handleFileSuccess = (value) => {
    if (value.code != 200) {
        modal.msgError(value.msg);
        return;
    }
    console.log(value.data);
   if(switchStore.isChat){
    addData.value.toId = chatStore.chatUser.userId
    addData.value.messageTime = getDate()
    addData.value.type = 3
    // console.log(bytesToSize(Number.parseInt(value.data.size)))
    addData.value.messageBody = value.data
    chatStore.chatSocket.sendP2PFileMessage(addData.value.toId,addData.value.messageBody,addData.value.type)

    value.data.size = bytesToSize(Number.parseInt(value.data.size))
    addData.value.messageBody = value.data
    console.log(addData.value)
    chatStore.onlineMsg.push({...addData.value})
   }

   if(switchStore.isGroupChat){
    addGroupData.value.groupId = chatStore.chatGroup.groupId
    addGroupData.value.messageTime = getDate()
    addGroupData.value.type = 3
    // console.log(bytesToSize(Number.parseInt(value.data.size)))

    console.log(addGroupData.value)
    var getInfoReq ={}
    getInfoReq.appId = addGroupData.value.appId
    getInfoReq.userId = addGroupData.value.fromId
    getInfo(getInfoReq).then((res)=>{
        if(res.code == 200){
            addGroupData.value.messageBody = value.data
            chatStore.chatSocket.sendGroupFileMessage(addGroupData.value.groupId,addGroupData.value.messageBody,addGroupData.value.type)

            value.data.size = bytesToSize(Number.parseInt(value.data.size))
            addGroupData.value.messageBody = value.data
            addGroupData.value.userData = res.data
            chatStore.onlineMsg.push({...addGroupData.value})
            console.log("chatStore.onlineMsg = ",chatStore.onlineMsg)
        }
    })
   }

}

// 获取文件大小
function bytesToSize(bytes) {
    if (bytes === 0) return '0 B';
    var k = 1024
    var sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
    var i = Math.floor(Math.log(bytes) / Math.log(k));
   return (bytes / Math.pow(k, i)).toPrecision(3) + '' + sizes[i];
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

function handleEntry(event) {
    let e = event.keyCode
    if (event.shiftKey && e === 13) {
    } else if (e === 13) {
        event.preventDefault()
        handelSend()
    }

}

function handleGroupEntry(event) {
    let e = event.keyCode
    // console.log(e)
    if (event.shiftKey && e === 13) {
    } else if (e === 13) {
        event.preventDefault()
        if(memberInfo.value.mute != null && memberInfo.value.mute == 1){
            modal.msgError("你已被禁言！");
            inputRef.value.innerHTML = null
            return 
        }
        handelGroupSend()
    }
}

function handleChatHistoryLog() {

    if (switchStore.isChat) {
        isAdd.value = true
        getFriendList()
    }
    if (switchStore.isGroupChat) {
        isGroupHistory.value = true
        getGroupList()
    }

}

async function getFriendList() {
    getFriendMsgReq.value.toId = chatStore.chatUser.userId,
    await chatFriendLogList(getFriendMsgReq.value).then((res) => {
        if (res.code == 200) {
            msglogList.value = res.data
        }
    })
}

async function getGroupList() {
    getGroupMsgReq.value.appId = chatStore.chatGroup.appId,
    getGroupMsgReq.value.groupId = chatStore.chatGroup.groupId,
    await chatGroupLogList(getGroupMsgReq.value).then((res) => {
        if (res.code == 200) {
            console.log(res.data);
            msglogList.value = res.data
        }
    })
}



function beforeUploadImg(file) {
  const fileSuffix = file.name.substring(file.name.lastIndexOf(".") + 1);
 
  const whiteList = ['xbm','tif','pjp','svgz','jpg','jpeg','ico','tiff','gif','svg','jfif','webp','png','bmp','pjpeg','avif'];
 
  if (whiteList.indexOf(fileSuffix) === -1) {
    modal.msgWarning('上传文件只能是图片格式');
    return false;
  }
}

function beforeUploadFile(file) {
  const fileSuffix = file.name.substring(file.name.lastIndexOf(".") + 1);
 
  const whiteList = ["pdf", "doc", "docx", "xls", "xlsx","txt"];
 
  if (whiteList.indexOf(fileSuffix) === -1) {
    modal.msgWarning('上传文件格式有误');
    return false;
  }
}


</script>

<style lang="scss" scoped>
.toolbar {
    width: 97%;
    height: 35px;
    // background-color: red;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 10px;
}

.tools {
    display: flex;
    height: 1.5rem;
    align-items: center;
}

.tool,
.tools img {
    margin: 0 5px;
    width: 1.5rem;
    cursor: pointer;
}

.input_send {
    width: 99%;
    height: 75px;
    border: 0;
    background: none;
    /* outline: none; */
}

.history img {
    width: 1.7rem;
    cursor: pointer;
}

.history img:hover {
    background-color: rgba(216, 216, 216, 0.4);
    transition: .3s;
    border-radius: 5px;
}

.avatar-uploader img:hover {
    background-color: rgba(216, 216, 216, 0.4);
    transition: .3s;
    border-radius: 5px;
}

.send_box {}

/* .send_box::-webkit-scrollbar { width: 0; height: 0; color: transparent; } */
.textArea {
    overflow-x: hidden;
    box-sizing: border-box;
    height: 120px;
    width: 100%;
    outline: none;
    border: none;
    padding: 0 10px;
    border: 0;
    border-radius: 5px;
    background: none;
    font-size: 1rem;
    padding: 10px;
    resize: none;

    img {
        width: 50px;
    }
}

.btn_box {
    display: flex;
    justify-content: flex-end;

    button {
        padding: 10px;
        border: 0;
        background-color: #b0e1f8;
        border-radius: 5px;
        margin: 0 8px;
        cursor: pointer;
    }
}

.chat_img {
    width: 1rem !important;
    height: auto;
}

.historyBox {
    margin-top: 2rem;
    width: 100%;
    height: 100%;

    ._message {
        width: 100%;
        height: auto;
        display: flex;
        align-items: center;
        border-bottom: #f5f5f5 1px solid;
        position: relative;
        margin: 1rem 0;
        padding: 10px 0;

        .imgbox {
            display: flex;
            margin: 0 15px 0 0;
            align-items: center;
        }

        .imgbox img {
            width: 2.5rem;
            height: 2.5rem;
            border-radius: 10px;
            object-fit: cover;
        }

        ._message_content_box {
            width: 64%;
            ._message_name {
                font-size: 0.3rem;
                margin-bottom: 4px;
                color: rgb(121, 163, 159);
            }
        }

        ._message_time {
            position: absolute;
            right: 0;
            color: rgb(121, 163, 159);
            font-size: 0.3rem;
        }

    }
}

.more {
    width: auto;
    display: flex;
    justify-content: center;
    cursor: pointer;
    margin: 0.5rem 0;
}
</style>