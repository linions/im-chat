<template>
    <div class="container">
        <div class="topbar">
            <div>
                视频聊天室
            </div>
            <!-- <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div> -->
        </div>

        <!-- v-if="chatStore.state == 3" -->
        <div   style=" width: 100%;height: 500px;  position: relative;"> 
            <video  ref="remoteVideo" autoplay playsinline class="friendVideo" style="width= 100%; height=100%; object-fit: fill"></video>
            <video  ref="localVideo"  autoplay playsinline class="selfVideo" style="width= 100%; height=100%; object-fit: fill"></video>
            <div class="topbar">
               
                <div style="margin-left: 100px;">
                    <span v-if="chatStore.friendShip != null">
                        {{chatStore.friendShip.remark == undefined || chatStore.friendShip.remark == null || chatStore.friendShip.remark == '' ? chatStore.chatUser.nickName : chatStore.friendShip.remark }}
                    </span>
                </div>
                <div style="margin-right: 100px;">
                    <span >
                        {{userStore.userInfo.nickName}}
                    </span>
                </div>
                <!-- <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div> -->
            </div>
        </div>

     
        <!-- <div v-else style=" width: 100%;height: 500px;  position: relative;"> 
            <img :src="chatStore.chatUser.photo" style="width: 200px;height: 200px;border-radius: 200px; margin: 50px 200px;">
            <div class="topbar">
                <div style="margin-left: 125px;">
                    <span v-if="chatStore.friendShip != null && chatStore.state == 2">
                       好友 
                       <span style="color: #8a98c9;">
                            {{chatStore.friendShip.remark == undefined || chatStore.friendShip.remark == null || chatStore.friendShip.remark == '' ? chatStore.chatUser.nickName : chatStore.friendShip.remark }}
                        </span>
                       请求与您视频聊天
                    </span>
                    <span v-if="chatStore.friendShip != null && chatStore.state == 1">
                       等待好友 
                       <span style="color: #8a98c9;">
                            {{chatStore.friendShip.remark == undefined || chatStore.friendShip.remark == null || chatStore.friendShip.remark == '' ? chatStore.chatUser.nickName : chatStore.friendShip.remark }}
                        </span>
                       接受视频聊天请求
                    </span>
                </div>
            </div> 
        </div>-->





        <div class="footerBar">
            <div v-if="chatStore.state == 1">
                <!-- <img  class="gcallButton" src="../../assets/img/accept.png" @click="sendCall()"> -->
                <img class="rcallButton" src="../../assets/img/hung.png" @click="cancelCall()">
            </div>
            <div v-if="chatStore.state == 2">
                <img  class="gcallButton" src="../../assets/img/accept.png" @click="acceptCall()">
                <img  class="rcallButton" src="../../assets/img/hung.png" @click="rejectAndHungCall()">
            </div>
            <div v-if="chatStore.state == 3">
                <img class="rcallButton" src="../../assets/img/hung.png" @click="hungUpCall()">
            </div>
        </div>


    </div>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import useUserStore from "../../store/modules/user";
import useSwitchStore from "../../store/modules/switch";
import modal from "../../plugins/modal";
import useChatStore from "../../store/modules/chat"
import { ElMessage, ElMessageBox } from 'element-plus';

const switchStore = useSwitchStore()
const chatStore = useChatStore()
const userStore = useUserStore()
var peer = null;

var mediaContent = ref({
    candidate:null,
    sdp:null,
})
var localVideo = ref();
var remoteVideo = ref();
var localStream = null;
var peerConnection = null;
//远程视频流参数
var mediaConstraints = {
    'mandatory': {
        'OfferToReceiveAudio': true,//音频
        'OfferToReceiveVideo': true//视频
    }
};
if(chatStore.state == 1){
    // 本地视频流绑定
    navigator.mediaDevices.getUserMedia({audio:true,video:true}).then(function(stream){
        localStream = stream;
        localVideo.value.srcObject = stream
        peerConnection = new RTCPeerConnection()
        stream.getTracks().forEach((track) => peerConnection.addTrack(track, stream));
        console.log("添加localStream视频流",stream);
        sendOffer()
    })
}


watch(() => chatStore.isReceive, async (value) => {

    if(chatStore.isReceive == true){
        console.log("收到answer 。。。。 ")
        console.log("chatStore.sdp = ",chatStore.sdp)
        console.log("chatStore.can = ",chatStore.candidate)
        setAnswer(chatStore.sdp);
        onCandidate(chatStore.candidate)
    }
},{
    immediate: true,
    deep: true
})


watch(() => chatStore.state, async (value) => {

if(chatStore.state == 0 && switchStore.isVideo == true){
    console.log("gsrggrs")
    switchStore.isVideo = false;
    if(localVideo.value != null && localVideo.value.srcObject != null){
        localVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
    }
    if(remoteVideo.value != null && remoteVideo.value.srcObject != null){
        remoteVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
    }
}
},{
immediate: true,
deep: true
})


function onCandidate(evt) {
    console.log("接收到Candidate...")
    var candidate = new RTCIceCandidate({
        sdpMLineIndex: evt.sdpMLineIndex,
        sdpMid: evt.sdpMid, 
        candidate: evt.candidate
    });
    peerConnection.addIceCandidate(candidate);
}


//---------------------- 处理连接 -----------------------
function prepareNewConnection() {
    var pc_config = { "iceServers": [] };
    try {
        peer = new webkitRTCPeerConnection(pc_config);
    }catch (e) {
        // console.log("建立连接失败，错误：" + e.message);
    }

    // 发送所有ICE候选者给对方
    peer.onicecandidate = function (evt) {
        if (evt.candidate) {
            sendCandidate({
                type: "candidate",
                sdpMLineIndex: evt.candidate.sdpMLineIndex,
                sdpMid: evt.candidate.sdpMid,
                candidate: evt.candidate.candidate
            });
        }
    };
    console.log('添加本地视频流...');
    peer.addStream(localStream);
    return peer;
}

function sendCandidate(candidate) {
    var text = JSON.stringify(candidate);
    mediaContent.value.candidate = text
    if(chatStore.state == 3 || chatStore.state == 2){
        console.log("发送candidate。。。。");
        chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,9,chatStore.messageKey,mediaContent.value)
    }else{
        chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,8,chatStore.messageKey,mediaContent.value)
    }
}

function sendOffer() {
    peerConnection = prepareNewConnection();
    peerConnection.createOffer(function (sessionDescription) { //成功时调用
        peerConnection.setLocalDescription(sessionDescription);
        console.log("发送offer: SDP");
        mediaContent.value.sdp = JSON.stringify(sessionDescription);
    }, function () {  //失败时调用
    }, mediaConstraints);
}

async function sendAnswer(evt) {
    peerConnection = prepareNewConnection();
    peerConnection.setRemoteDescription(new RTCSessionDescription(evt));
    peerConnection.addEventListener("addstream", onRemoteStreamAdded, false);
    peerConnection.addEventListener("removestream", onRemoteStreamRemoved, false);

    // 当接收到远程视频流时，使用本地video元素进行显示
    function onRemoteStreamAdded(event) {
        remoteVideo.value.srcObject = event.stream;
        console.log("添加远程视频流",remoteVideo.value.srcObject);
    }

    // 当远程结束通信时，取消本地video元素中的显示
    function onRemoteStreamRemoved(event) {
        console.log("移除远程视频流");
        remoteVideo.value.srcObject = null;
    }

    peerConnection.createAnswer(function (sessionDescription) {//成功时
        peerConnection.setLocalDescription(sessionDescription);
        console.log("发送answer: SDP");
        console.log(sessionDescription);
        mediaContent.value.sdp = JSON.stringify(sessionDescription);
    }, function (err) { //失败时
        console.log("发送answer: SDP   失败",err);
    }, mediaConstraints);
}

function setAnswer(evt) {
    // evt = JSON.parse(evt)
    if (!peerConnection) {
        peerConnection = new RTCPeerConnection()
        // stream.getTracks().forEach((track) => peerConnection.addTrack(track, localStream));
    }
    console.log("setAnswer(evt) " ,evt)
    peerConnection.setRemoteDescription(new RTCSessionDescription(evt,"answer"));

    peerConnection.addEventListener("addstream", onRemoteStreamAdded, false);
    peerConnection.addEventListener("removestream", onRemoteStreamRemoved, false);

    // 当接收到远程视频流时，使用本地video元素进行显示
    function onRemoteStreamAdded(event) {
        remoteVideo.value.srcObject = event.stream;
        console.log("添加远程视频流",remoteVideo.value.srcObject);
    }

    // 当远程结束通信时，取消本地video元素中的显示
    function onRemoteStreamRemoved(event) {
        console.log("移除远程视频流");
        remoteVideo.value.srcObject = null;
    }
}

//---------------------- 视频处理 -----------------------
function acceptCall() {
    // alert("接受")
    console.log("chatStore.state = ",chatStore.state)
    console.log("chatStore.sdp = ",chatStore.sdp)
    console.log("chatStore.can = ",chatStore.candidate)

    navigator.mediaDevices.getUserMedia({audio:true,video:true}).then(function(stream){
        localStream = stream;
        localVideo.value.srcObject = stream
        peerConnection = new RTCPeerConnection()
        // localStream.getTracks().forEach((track) => peerConnection.addTrack(track, stream));
        console.log("添加localStream视频流",stream);
        sendAnswer(chatStore.sdp)
        onCandidate(chatStore.candidate)
        chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,3,chatStore.messageKey,null)
    })
}

function hungUpCall(){
    // alert("挂断")
    if(localVideo.value.srcObject != null && remoteVideo.value.srcObject != null){
        localVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
        remoteVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
    }
    chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,5,chatStore.messageKey,null)
    switchStore.isVideo = false
    switchStore.isChat = true
    chatStore.state = 0;
    chatStore.messageKey = null
    chatStore.isReceive = false

}

function rejectAndHungCall(){
//    alert("拒绝")
   if(localVideo.value.srcObject != null && remoteVideo.value.srcObject != null){
        localVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
        remoteVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
    }
   chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,4,chatStore.messageKey,null)
   switchStore.isVideo = false
   switchStore.isChat = true
    chatStore.state = 0;
    chatStore.messageKey = null
    chatStore.isReceive = false
}

function cancelCall(){
    // alert("取消")
    if(localVideo.value.srcObject != null && remoteVideo.value.srcObject != null){
        localVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
        remoteVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
    }
   chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,6,chatStore.messageKey,null)
    switchStore.isVideo = false
    switchStore.isChat = true
    chatStore.state = 0;
    chatStore.messageKey = null
    chatStore.isReceive = false
}




</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');


.topbar {
    width: 95%;
    height: 70px;
    // background-color: red;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    color: grey;
    font-size: 1.5rem;
    font-family: 'Bebas Neue', cursive;
    // border-bottom: rgba(121, 163, 159, 0.3) 1px solid;
    letter-spacing: 2px;
}
.friendVideo{
    width: 320px; 
    height: 350px;
    margin-left: 10px;
    display: inline-block;
    border-radius: 5px;
}

.selfVideo{
    margin-top: 50px;
    width: 320px; 
    height: 350px; 
    margin-left: 10px;

    // position: absolute;
    right: 0;
    border-radius: 5px;
    // position: absolute;
}

.bottomBox{
    height: 30%;
    background: #000;
}

.container {
    width: 100%;
    animation: ani 1s;
    position: relative;
}

@keyframes ani {
    0% {
        transform: translateX(600px);
    }

    100% {
        transform: translateX(0px);
    }
}

.message_img {
    width: 150px !important;
    height: 150px !important;
    background-color: #fff;
    border-radius: 50%;
    overflow: hidden;
    object-fit: cover;
    /* border-radius: 50%!important; */
}

.button{
    width: 100px;
    height:50px;
    font-size: 20px;
    border-radius: 10px;
    background-color: #8a98c9;
    color: white;
}

.container2 {
    width: 100%;
    animation: ani2 1.2s;
}

@keyframes ani2 {
    0% {
        transform: translateX(0px);
    }

    100% {
        transform: translateX(700px);
    }
}


.topbar {
    width: 95%;
    height: 70px;
    // background-color: red;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    color: grey;
    font-size: 1.5rem;
    font-family: 'Bebas Neue', cursive;
    border-bottom: rgba(121, 163, 159, 0.3) 1px solid;
    letter-spacing: 2px;
}

.footerBar{
    width: 100%;
    height: 208px;
    text-align: center;
    line-height: 70px;
    position: absolute;
    top: 550px;
    color: grey;
    font-size: 1.5rem;
    font-family: 'Bebas Neue', cursive;
    // border-top: rgba(36, 41, 41, 0.3) ;
    letter-spacing: 2px;
}

.rcallButton {
    width: 80px;
    height: 80px;
    margin: 50px;
    border-radius: 40px;
}
.gcallButton {
    width: 80px;
    height: 80px;
    margin: 50px;
    border-radius: 40px;
}

.rcallButton:hover{
    background-color: red;
}

.gcallButton:hover{
    background-color: green;
    
}
</style>