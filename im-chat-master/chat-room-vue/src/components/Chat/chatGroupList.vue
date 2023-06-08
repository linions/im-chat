<template>
    <div class="chat_list" ref="chatBox">
        <div ref="isLoad" class="loader_box">
            <Loader></Loader>
        </div>

        <div v-if="switchStore.isGroupChat" ref="isGroupShow" style="display: none">

            <!-- <div v-if="isMore" class="more" @click="handelMoreList">
                <div><i class="fa fa-clock-o" style="margin-right: 10px;"></i>
                    <span>查看更多聊天记录</span>
                </div>
            </div> -->

            <!-- 聊天记录 -->
            <div v-for="(item,index) in chatStore.groupChatLogList" :key="index">
                <div style="margin-left: 260px;"><i class="fa fa-clock-o" style="margin-right: 10px;"></i>
                    <span>{{item.messageTime}}</span>
                </div>
                <div v-if="item.fromId != userStore.userInfo.userId && item.groupId == chatStore.chatGroup.groupId"  class="chat_other_box">
                    <div class="chat_img_box">
                        <img class="chat_img" v-if="item.userData.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleGroupMemberImg(item.userData)">
                        <img class="chat_img" v-else :src=item.userData.photo alt="" @click="handleGroupMemberImg(item.userData)">
                    </div>
                     <!-- 消息 -->
                     <div v-if="item.type == 1" v-html="item.messageBody"  class="message_other_box"></div>
                    <div  class="message_other_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody)" style="width: 10rem!important;height: auto;" :src=item.messageBody alt="">
                    </div>
                    <div v-else-if="item.type == 3" class="message_other_box" @click="downloadFile(item.fileDto)">
                        <a :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{ item.fileDto.size }}</span>
                            <div style="color:black"> {{ item.fileDto.name }}</div>
                        </a>
                    </div>
                    <div v-else-if="item.type == 4" class="message_other_box" >
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_other_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <!-- <el-icon @click=downloadFile(item.fileDto) v-if="item.type == 3" :size="30" style="color: #8a98c9;margin-top: 40px;margin-left: 10px;">
                    </el-icon> -->
                </div>
                <div v-if="item.fromId == userStore.userInfo.userId && item.groupId == chatStore.chatGroup.groupId"
                    class="chat_self_box">
                     <!-- 消息 -->
                    <div v-if="item.type == 1" v-html="item.messageBody"  class="message_self_box"></div>
                    <div  class="message_self_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody)" style="width: 10rem!important;height: auto;" :src=item.messageBody alt="">
                    </div>
                    <div v-else-if="item.type == 3" class="message_self_box" @click="downloadFile(item.fileDto)">
                        <a :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{ item.fileDto.size }}</span>
                            <div style="color:black"> {{ item.fileDto.name }}</div>
                        </a>
                    </div>
                    <div v-else-if="item.type == 4" class="message_self_box" >
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_self_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>

                    <div class="chat_img_box">
                        <img class="chat_img" v-if="userStore.userInfo.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleimg(userStore.userInfo)">
                        <img class="chat_img" v-else :src=userStore.userInfo.photo alt="" @click="handleimg(userStore.userInfo)">
                    </div>
                </div>
            </div>

            <!-- 实时聊天列表 -->
            <div v-for="(item,index) in chatStore.onlineMsg" :key="index">
                <div style="margin-left: 260px;"><i class="fa fa-clock-o" style="margin-right: 10px;"></i>
                    <span>{{item.messageTime}}</span>
                </div>
                <div v-if="item.fromId != userStore.userInfo.userId && item.groupId == chatStore.chatGroup.groupId"  class="chat_other_box">
                    <div class="chat_img_box">
                        <img class="chat_img" v-if="item.userData.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleGroupMemberImg(item.userData)">
                        <img class="chat_img" v-else :src=item.userData.photo alt="" @click="handleGroupMemberImg(item.userData)">
                    </div>
                   <!-- 消息 -->
                   <div v-if="item.type == 1" v-html="item.messageBody"  class="message_other_box"></div>
                    <div  class="message_other_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody.url)" style="width: 10rem!important;height: auto;" :src=item.messageBody.url alt="">
                    </div>
                    <div v-else-if="item.type == 3" class="message_other_box" @click="downloadFile(item.messageBody)">
                        <a :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{item.messageBody.size}}</span>
                            <div style="color:black">{{item.messageBody.name}}</div>
                        </a>
                    </div>
                    <div v-else-if="item.type == 4" class="message_other_box">
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_other_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <!-- <el-icon @click=downloadFile(item.messageBody) v-if="item.type == 3" :size="30" style="color: #8a98c9;margin-top: 40px;margin-left: 10px;">
                        
                    </el-icon> -->
                </div>
                <div v-if="item.fromId == userStore.userInfo.userId && item.groupId == chatStore.chatGroup.groupId"
                    class="chat_self_box">
                    <el-icon v-if="item.error" style="color: red;margin-top: 25px;"><WarningFilled /></el-icon>

                    <!-- 消息 -->
                    <div v-if="item.type == 1" v-html="item.messageBody"  class="message_self_box"></div>
                    <div  class="message_self_box" v-else-if="item.type == 2">
                        <img @click="handlePictureCardPreview(item.messageBody.url)" style="width: 10rem!important;height: auto;" :src=item.messageBody.url alt="">
                    </div>
                    <div  v-else-if="item.type == 3" class="message_self_box" style="height: 60px;" @click="downloadFile(item.messageBody)">
                        <a :href=fileUrl target="_Blank" style="text-decoration:none;display: inherit;">
                            <img style="width: 40px!important;height: 40px;" src='../../assets/img/file.png' alt="">
                            <span class="fileSize">{{item.messageBody.size}}</span>
                            <div style="color:black">{{item.messageBody.name}}</div>
                        </a>
                    </div>
                    <div v-else-if="item.type == 4" class="message_self_box">
                        <img style="width: 30px!important;height: 30px;" src='../../assets/img/talk.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <div v-else-if="item.type == 5" class="message_self_box">
                        <img style="width: 30px!important;height: 30px;margin-right: 10px;" src='../../assets/img/video.png' alt="">
                        <span v-if="item.messageBody == '6'" class="messageBox"> 已取消 </span>
                        <span v-if="item.messageBody == '5'" class="messageBox"> 时长：  </span>
                        <span v-if="item.messageBody == '4'" class="messageBox"> 已拒绝 </span>
                    </div>
                    <div class="chat_img_box">
                        <img class="chat_img" v-if="userStore.userInfo.photo == null" src="../../assets/img/profile.jpg" alt="" @click="handleimg(userStore.userInfo)">
                        <img class="chat_img" v-else :src=userStore.userInfo.photo alt="" @click="handleimg(userStore.userInfo)">
                    </div>
                </div>
            </div>

            <el-dialog v-model="dialogVisible" title="预览" width="800px" append-to-body>
                <img :src="dialogImageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
            </el-dialog>

        </div>


    </div>
</template>

<script setup>
import { ref, watch,nextTick,reactive } from 'vue'
import useChatStore from "../../store/modules/chat";
import useSwitchStore from '../../store/modules/switch'
import useUserStore from '../../store/modules/user'
import { chatGroupLogList } from '../../api/chatLog/chatLog'
import Loader from '../../components/Login/loader.vue'
import {getRelation} from '../../api/friends/friend'
import useViewStore from '../../store/modules/viewInfo';

const viewStore = useViewStore()
const switchStore = useSwitchStore()
const chatStore = useChatStore()
const userStore = useUserStore()

const chatBox = ref();
const isLoad = ref();
const isGroupShow = ref();

const getGroupMsgReq = reactive({
    groupId: chatStore.chatGroup.groupId,
    appId: chatStore.chatGroup.appId,
    operator: userStore.userInfo.userId
})
const dialogVisible = ref()
const dialogImageUrl = ref()

watch(() => chatStore.chatGroup, async (value) => {

    nextTick(() => {
        isGroupShow.value.style.display = `none`
        isLoad.value.style.display = `block`
    })
    await getGroupChatList();
    nextTick(() => {
        setTimeout(() => {
            setTimeout(async () => {
                isLoad.value.style.display = `none`
            }, 100);
            isGroupShow.value.style.display = `block`
            isGroupShow.value.scrollIntoView({ block: "end" })
        }, 100);
    })

},
    {
        immediate: true,
        deep: true
    })

// 聊天记录
async function getGroupChatList() {
    await chatGroupLogList(getGroupMsgReq).then((res) => {
        if (res.code == 200) {
            chatStore.groupChatLogList = res.data
            chatStore.onlineMsg = []
        }
    })
}

const fileUrl = ref()

function downloadFile(item){
    fileUrl.value = "http://" + chatStore.chatSocket.ip + ":8081/src/assets/data/" + item.name
}


let getChatReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})
watch(() => chatStore.onlineMsg, async (value) => {
    if(chatStore.onlineMsg.length != 0){
        setTimeout(function () {
            viewStore.getConversationList(getChatReq.value)
        },2000)
        nextTick(() => {
            isGroupShow.value.scrollIntoView({ block: "end" })
        })
    }
},
{
    immediate: true,
    deep: true
})


  // 预览图片
function handlePictureCardPreview(url) {
    dialogImageUrl.value = url
    dialogVisible.value = true
}


function handleGroupMemberImg(v) {

    const getRelationReq = ref({
        appId: userStore.userInfo.appId,
        fromId: userStore.userInfo.userId,
        toId: v.userId
    })

    getRelation(getRelationReq.value).then((res) => {
        if (res.code == 200) {
            var friendData = {...res.data}
            friendData.param = {}
            friendData.param.userInfo = {...v}
            console.log("friendData = ",friendData)
            switchStore.FriendIntroData = friendData

            switchStore.isPersonal = false
            switchStore.isFriends = false
            switchStore.isChat = false
            switchStore.isGroupChat = false
            switchStore.isNotification = false
            switchStore.isFriendIntro = true
        }else{
            var friendData = {}
            friendData.remark = null
            friendData.status = 0
            friendData.black = 1
            friendData.param = {}
            friendData.param.userInfo = {...v}
            console.log("friendData = ",friendData)
            switchStore.FriendIntroData = friendData

            switchStore.isPersonal = false
            switchStore.isFriends = false
            switchStore.isChat = false
            switchStore.isGroupChat = false
            switchStore.isFriendIntro = true
            switchStore.isNotification = false

        }
    })
}

function handleimg(v) {
    switchStore.isFriendIntro = false
    switchStore.isPersonal = true
    switchStore.isFriends = false
    switchStore.isChat = false
    switchStore.isVideo = false
    switchStore.isGroupChat = false
    switchStore.isNotification = false

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

.fileSize{
    float: right;
    margin-top: 12px;
    margin-left: 5px;
}

.message_box {
    padding: 10px;
    width: auto;
    height: auto;
    background-color: #e5e5e5;
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