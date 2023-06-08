<template>
  <el-row v-if="switchStore.isVoice" :gutter="12">
      <el-col  :span="8">
          <el-card class="voiceBox" shadow="always">
            <span v-if="chatInfo.chatUser">
              <img :src="chatInfo.chatUser.photo" style="width: 100px;height: 100px;border-radius: 100px; margin:10px 30px 10px 30px;">
            </span>
            <span v-if="chatInfo.friendship != null" style="color: gray;font-size: 20px;text-align: center;width: 0%;margin: 0 30px 0 50px;">
                {{chatInfo.friendship.remark == undefined || chatInfo.friendship.remark == null || chatInfo.friendship.remark == '' ? chatInfo.chatUser.nickName : chatInfo.friendship.remark }}
            </span>
           
            
            <div class="footerBar" style="margin: 30px 10px 0;">
              <div v-if="chatStore.state == 1">
                  <img class="rcallButton" style="margin-left: 40px;" src="./assets/img/hung.png" @click="cancelCall()">
              </div>
              <div v-if="chatStore.state == 2">
                  <img  class="gcallButton" src="./assets/img/accept.png" @click="acceptCall()">
                  <img  class="rcallButton" src="./assets/img/hung.png" @click="rejectAndHungCall()">
              </div>
              <div v-if="chatStore.state == 3">
                  <img class="rcallButton" style="margin-left: 40px;" src="./assets/img/hung.png" @click="hungUpCall()">
              </div>
              <video  ref="remoteVideo" autoplay playsinline class="friendVideo"></video>
              <video  ref="localVideo"  autoplay playsinline class="selfVideo"></video>
          </div>
          </el-card>
      </el-col>
  </el-row>
  <RouterView></RouterView>
  
</template>

<script setup>
import { onMounted, ref,watch,computed } from 'vue'
import useSwitchStore from "./store/modules/switch";
import useUserStore from "./store/modules/user";
import useChatStore from "./store/modules/chat"
import Cookies from 'js-cookie'
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
var mediaConstraints = {
    'mandatory': {
        'OfferToReceiveAudio': true,
        'OfferToReceiveVideo': false
    }
};

const chatInfo = ref({
    friendship:null,
    chatUser:null
})

watch(() => chatStore.state, async (value) => {
    chatInfo.value.chatUser = chatStore.chatUser
    chatInfo.value.friendship = chatStore.friendShip
    // console.log(chatInfo.value)
    // console.log("state = ",chatStore.state)
    if(chatStore.state == 1 && chatStore.isVoice == true){
        navigator.mediaDevices.getUserMedia({audio:true,video:false}).then(function(stream){
            localStream = stream;
            localVideo.value.srcObject = stream
            peerConnection = new RTCPeerConnection()
            stream.getTracks().forEach((track) => peerConnection.addTrack(track, stream));
            console.log("添加localStream视频流",stream);
            sendOffer()
        })
    }
    
},{
    immediate: true,
    deep: true
})




watch(() => chatStore.isReceive, async (value) => {
  if(chatStore.isReceive == true && chatStore.isVoice == true){
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
    //   stream.getTracks().forEach((track) => peerConnection.addTrack(track, localStream));
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
//   alert("接受")
  console.log("chatStore.state = ",chatStore.state)
  console.log("chatStore.sdp = ",chatStore.sdp)
  console.log("chatStore.can = ",chatStore.candidate)
  chatInfo.value.chatUser = chatStore.chatUser
  chatInfo.value.friendship = chatStore.friendShip

navigator.mediaDevices.getUserMedia({audio:true,video:false}).then(function(stream){
    localStream = stream;
    localVideo.value.srcObject = stream
    peerConnection = new RTCPeerConnection()
    // stream.getTracks().forEach((track) => peerConnection.addTrack(track, stream));
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
    switchStore.isVoice = false
    chatStore.state = 0;
    chatStore.messageKey = null
    chatStore.isReceive = false

}

function rejectAndHungCall(){
    // alert("拒绝")
    if(localVideo.value.srcObject != null && remoteVideo.value.srcObject != null){
        localVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
        remoteVideo.value.srcObject.getTracks().forEach(function(track) {
            track.stop();
        });
    }
    chatStore.chatSocket.sendP2PVideo(chatStore.chatUser.userId,4,chatStore.messageKey,null)
    switchStore.isVoice = false
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
    switchStore.isVoice = false
    chatStore.state = 0;
    chatStore.messageKey = null
    chatStore.isReceive = false
}

</script>

<style scoped>
.voiceBox{
  position: absolute;
  margin-left: 1150px;
  width: 200px;
  height: 300px;
  border-radius: 40px;
  /* background-color:#b3bbda ; */
}

/* .footerBar{
    width: 100%;
    height: 30px;
    text-align: center;
    line-height: 70px;
    position: absolute;
    top: 550px;
    color: grey;
    font-size: 1.5rem;
    font-family: 'Bebas Neue', cursive;
    border-top: rgba(36, 41, 41, 0.3) ;
    letter-spacing: 2px;
} */

.rcallButton {
    width: 60px;
    height: 60px;
    /* margin: 50px; */
    border-radius: 60px;
}
.gcallButton {
    width: 60px;
    height: 60px;
    margin-right: 20px;
    border-radius: 60px;
}

.rcallButton:hover{
    background-color: red;
}

.gcallButton:hover{
    background-color: green;
    
}
</style>
