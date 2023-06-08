<template>
    <div class="topBar">
        <div class="topBarBox">
            <div class="topBar_name">
                <div v-if="switchStore.isChat">
                    <span v-if="chatStore.friendShip!= null && chatStore.friendShip.remark!= null">
                        {{chatStore.friendShip.remark}}
                    </span>
                    <span v-else>
                        {{chatStore.chatUser.nickName}}
                    </span>
                </div>
                <div v-if="switchStore.isGroupChat">
                    <span v-if="chatStore.chatGroup.groupName != null">
                        {{chatStore.chatGroup.groupName}}
                    </span>
                </div>
            </div>
            <div class="topBar_tool" v-if="switchStore.isChat">
                <img  src="../../assets/img/phone.png" @click="handleVoiceChat()" alt="">
                <img src="../../assets/img/video.png" @click="handleVideoChat()" alt="">
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref,reactive ,watch} from 'vue';
import useSwitchStore from "../../store/modules/switch";
import useChatStore from "../../store/modules/chat";
import moadl from '../../plugins/modal'

const switchStore = useSwitchStore()
const chatStore = useChatStore()


function handleVideoChat(){
    if(chatStore.friendShip.status != 1){
        moadl.msgError("非好友关系，无法进行视频通话")
    }else{
        switchStore.isVideo = true
        switchStore.isNotification = false
        switchStore.isChat = false
        chatStore.state = 1
        chatStore.isVoice = false
        chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,2,null,null)
    }
}

function handleVoiceChat(){
    if(chatStore.friendShip.status != 1){
        moadl.msgError("非好友关系，无法进行语音通话")
    }else{
        switchStore.isVoice = true
        chatStore.state = 1
        chatStore.isVoice = true
        chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,1,null,null)
    }
}


</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

.topBar {
    width: 100%;
    height: 70px;
    /* background-color: red; */
    border-bottom: rgba(121, 163, 159, 0.3) 1px solid;
    display: flex;
    align-items: center;
}

.topBarBox {
    width: 100%;
    padding: 0 20px;
    display: flex;
    justify-content: space-between;
}

.topBar_name {
    font-weight: bold;
    font-size: 1.5rem;
    color: grey;
    letter-spacing: 2px;
    font-family: 'Bebas Neue', cursive;
}
.topBar_tool img{
    width: 2rem;
    margin: 0 5px;
    cursor: pointer;
}

.topBar_tool img:hover {
    background-color: rgba(216, 216, 216, 0.4);
    transition: .3s;
    border-radius: 5px;
}
</style>