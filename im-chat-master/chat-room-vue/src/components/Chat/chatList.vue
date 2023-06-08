<template>
    <div class="chat_list" ref="chatBox">
        <div ref="isLoad" class="loader_box">
            <Loader></Loader>
        </div>

        <div v-if="switchStore.isChat" ref="isShow" style="display: none">
            <div v-for="(item,index) in chatStore.chatLogList" :key="index">
                <div>
                    <div style="margin-left: 260px;">
                        <i class="fa fa-clock-o" style="margin-right: 10px;"></i>
                        <span>{{item.messageTime}}</span>
                    </div>
                </div>
                <div v-if="item.fromId == chatStore.chatUser.userId && item.toId == userStore.userInfo.userId"
                    class="chat_other_box">
                    
                    <div class="chat_img_box">
                        <img class="chat_img" v-if="chatStore.chatUser.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleFriendImg(chatStore.chatUser)">
                        <img class="chat_img" v-else :src=chatStore.chatUser.photo alt="" @click="handleFriendImg(chatStore.chatUser)">
                    </div>
                    <!-- 消息 -->
                    <div v-if="item.type == 1" v-html="item.messageBody"  class="message_other_box"></div>
                    <div  class="message_other_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody)" style="width: 10rem!important;height: auto;" :src=item.messageBody alt="">
                    </div>
                    <div v-else-if="item.type == 3" @click="downloadFile(item.fileDto)" class="message_other_box" >
                        <a  :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{ item.fileDto.size }}</span>
                            <div style="color:black"> {{ item.fileDto.name }}</div>
                        </a>
                        
                    </div>
                    <div v-else-if="item.type == 4" class="message_other_box" >
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-else-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }}</span>
                        <span v-else-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话结束 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_other_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-else-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }} </span>
                        <span v-else-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话结束 </span>
                    </div>
                </div>
                <div  v-if="item.fromId == userStore.userInfo.userId && item.toId == chatStore.chatUser.userId" class="chat_self_box">
                    <!-- 消息 -->
                    <div v-if="item.type == 1" v-html="item.messageBody"  class="message_self_box"></div>
                    <div  class="message_self_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody)" style="width: 10rem!important;height: auto;" :src=item.messageBody alt="">
                    </div>
                    <div v-else-if="item.type == 3" @click="downloadFile(item.fileDto)" class="message_self_box">
                        <a  :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{ item.fileDto.size }}</span>
                            <div style="color:black"> {{ item.fileDto.name }}</div>
                        </a>
                    </div>
                    <div v-else-if="item.type == 4"  class="message_self_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-else-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }}  </span>
                        <span v-else-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话结束 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_self_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-else-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }}  </span>
                        <span v-else-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话结束 </span>
                    </div>


                    <div class="chat_img_box">
                        <img class="chat_img" v-if="userStore.userInfo.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleimg(userStore.userInfo)">
                        <img class="chat_img" v-else :src=userStore.userInfo.photo alt="" @click="handleimg(userStore.userInfo)">
                    </div>
                </div>
            </div>

            <!-- 好友在线时 v-if="!isOnline && selfMsgList.length != 0"-->
            <div v-for="(item,index) in chatStore.onlineMsg" :key="index">
                <div>
                    <div style="margin-left: 260px;">
                        <i class="fa fa-clock-o" style="margin-right: 10px;"></i>
                        <span>{{item.messageTime}}</span>
                    </div>
                </div>
                <div  v-if="item.fromId == userStore.userInfo.userId && item.toId == chatStore.chatUser.userId" class="chat_self_box">
                    <el-icon v-if="item.error" style="color: red;margin-top: 25px;"><WarningFilled /></el-icon>
                    <!-- 消息 -->
                    <div v-if="item.type == 1" v-html="item.messageBody"  class="message_self_box"></div>
                    <div  class="message_self_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody.url)" style="width: 10rem!important;height: auto;" :src=item.messageBody.url alt="">
                    </div>
                    <div v-else-if="item.type == 3" @click="downloadFile(item.messageBody)" class="message_self_box" style="height: 60px;">
                        <a :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{item.messageBody.size}}</span>
                            <div style="color:black">{{item.messageBody.name}}</div>
                        </a>
                    </div>
                    <div v-else-if="item.type == 4" class="message_self_box" style="height: 60px;">
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-else-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }}</span>
                        <span v-else-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话进行中 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_self_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-else-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }}</span>
                        <span v-else-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话进行中 </span>
                    </div>

                    <div class="chat_img_box">
                        <img class="chat_img" v-if="userStore.userInfo.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleimg(userStore.userInfo)">
                        <img class="chat_img" v-else :src=userStore.userInfo.photo alt="" @click="handleimg(userStore.userInfo)">
                    </div>
                </div>
                <div v-if="item.fromId == chatStore.chatUser.userId && item.toId == userStore.userInfo.userId"
                    class="chat_other_box">
                    <div class="chat_img_box">
                        <img class="chat_img" v-if="chatStore.chatUser.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleFriendImg(chatStore.chatUser)">
                        <img class="chat_img" v-else :src=chatStore.chatUser.photo alt="" @click="handleFriendImg(chatStore.chatUser)">
                    </div>
                    <!-- 消息 -->
                    <div v-if="item.type == 1" v-html="item.messageBody"  class="message_other_box"></div>
                    <div  class="message_other_box" v-else-if="item.type == 2 && item.messageBody != null">
                        <img @click="handlePictureCardPreview(item.messageBody.url)" style="width: 10rem!important;height: auto;" :src=item.messageBody.url alt="">
                    </div>
                    <div v-else-if="item.type == 3" @click="downloadFile(item.messageBody)" class="message_other_box" style="height: 60px;">
                        <a :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{item.messageBody.size}}</span>
                            <div style="color:black">{{item.messageBody.name}}</div>
                        </a>
                        
                    </div>
                    <div v-else-if="item.type == 4" class="message_other_box" style="height: 60px;">
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }} </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话进行中 </span>

                    </div>
                    <div v-else-if="item.type == 5" class="message_other_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长: {{ item.time }} </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                        <span v-else class="messageBox"> 通话进行中 </span>

                    </div>
                </div>
                
            </div>

        </div>

        <el-dialog v-model="dialogVisible" title="图片预览" width="500px"  append-to-body>
            <img :src="dialogImageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
        </el-dialog>
                
    </div>
</template>

<script setup>
import {ref, watch,nextTick,} from 'vue'
import useChatStore from "../../store/modules/chat";
import useSwitchStore from '../../store/modules/switch'
import useSocketChatStore from '../../store/modules/socketchat'
import useUserStore from '../../store/modules/user'
import { chatFriendLogList } from '../../api/chatLog/chatLog'
import Loader from '../../components/Login/loader.vue'
import useViewStore from '../../store/modules/viewInfo';


const switchStore = useSwitchStore()
const chatStore = useChatStore()
const userStore = useUserStore()
const viewStore = useViewStore()
const SocketChatStore = useSocketChatStore()

const chatBox = ref();
const isShow = ref();
const isLoad = ref();

let isOnline = ref(false);



let getListData = ref({
    fromId: userStore.userInfo.userId,
    toId: chatStore.chatUser.userId,
    ownerId: userStore.userInfo.userId,
    appId:userStore.userInfo.appId
})


const dialogVisible = ref()
const dialogImageUrl = ref()

// 预览图片
function handlePictureCardPreview(url) {
    dialogImageUrl.value = url
    dialogVisible.value = true
}

const fileUrl = ref()

function downloadFile(item){
    fileUrl.value = "http://" + chatStore.chatSocket.ip + ":8081/src/assets/data/" + item.name
}


watch(() => chatStore.chatUser, async (value) => {
    
    SocketChatStore.chatOnlineUsers.forEach(function (item, index) {
        
        if (item.userGuid == chatStore.chatUser.friendGuId) {
            isOnline.value = true
        }
    })

    nextTick(() => {
        isShow.value.style.display = `none`
        isLoad.value.style.display = `block`
    })

    if (switchStore.isChat) {
        getListData.value.toId = chatStore.chatUser.userId
        getListData.value.fromId = userStore.userInfo.userId
        await getFriendChatList()
    }
    nextTick(() => {
        setTimeout(async () => {
            isLoad.value.style.display = `none`
        }, 100);
        setTimeout(() => {
            isShow.value.style.display = `block`
            isShow.value.scrollIntoView({ block: "end" })
        }, 100);
    })
},

    {
        immediate: true,
        deep: true
    })


async function getFriendChatList() {
    await chatFriendLogList(getListData.value).then((res) => {
        if (res.code == 200) {
            chatStore.chatLogList = res.data
            chatStore.onlineMsg = []
        }
    })
}

let getChatReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

watch(() => chatStore.onlineMsg, async (value) => {
    console.log("chatStore.onlineMsg changing ",value)
    if(chatStore.onlineMsg.length != 0){
        setTimeout(function () {
            viewStore.getConversationList(getChatReq.value)
        },2000)
        nextTick(() => {
            isShow.value.scrollIntoView({ block: "end" })
        })
    }
},
{
    immediate: true,
    deep: true
})


function handleimg(v) {
    switchStore.isFriendIntro = false
    switchStore.isPersonal = true
    switchStore.isFriends = false
    switchStore.isChat = false
    switchStore.isGroupChat = false
    switchStore.isNotification = false
}

function handleFriendImg(v) {
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isChat = false
    switchStore.isGroupChat = false
    switchStore.isNotification = false

    var friendData = {...chatStore.friendShip}
    friendData.param = {}
    friendData.param.userInfo = {...chatStore.chatUser}
    console.log("friendData = ",friendData)
    switchStore.FriendIntroData = friendData
    switchStore.isFriendIntro = true
}


</script>

<style scoped>
.chat_list {
    width: 95.5%;
    height: 400px;
    border-bottom: rgba(121, 163, 159, 0.3) 1px solid;
    overflow-y: scroll;
    padding: 15px;
}


.chat_other_box {
    display: flex;
    margin-bottom: 20px;
}

.chat_self_box {
    display: flex;
    margin-bottom: 20px;
    justify-content: flex-end;
}

.chat_img_box {
    width: 45px;
    height: 45px;
    border-radius: 50%;
    background-color: #fff;
    cursor: pointer;
}

.chat_img {
    width: 45px;
    height: 45px;
    object-fit: cover;
    border-radius: 50%;
}

.message_box {
    padding: 10px;
    width: auto;
    height: auto;
    background-color: #8a98c9;;
    margin-top: 10px;
    margin-left: 5px;
    margin: 10px 5px;
    border-radius: 10px;
    word-break: break-all;
}

.message_self_box {
    padding: 10px;
    width: auto;
    height: auto;
    margin-top: 10px;
    margin-left: 5px;
    margin: 10px 5px;
    border-radius: 10px;
    background-color: #8a98c9 !important;
    word-break: break-all;
}
.message_other_box {
    padding: 10px;
    width: auto;
    height: auto;
    margin-top: 10px;
    margin-left: 5px;
    margin: 10px 5px;
    border-radius: 10px;
    background-color: #d0d4e8 !important;
    word-break: break-all;
}

.messageBox{
    float: right; 
    margin-top: 5px;
}

.fileSize{
    float: right;
    margin-top: 12px;
    margin-left: 5px;
}

.more {
    width: auto;
    display: flex;
    justify-content: center;
    cursor: pointer;
    margin: 0.5rem 0;
}

.loader_box {
    position: fixed;
    height: 100%;
    left: 60%;
}

.loader span {
    width: 100px !important;
    height: 100px !important;
    transition: 0.3s;
}
</style>