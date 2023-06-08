<template>
    <div class="container1">
        <div class="topbar">
            <div>组群申请</div>
            <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>
        </div>

        <div class="Box">
            <div class="requestBox" v-for="(item,index) in dataList" :key="index" >
                <div class="imgbox" @click="showGroupIntro(item.param.groupInfo)">
                    <img v-if="item.param.groupInfo.photo != null && item.param.groupInfo.photo != ''" :src=item.param.groupInfo.photo alt="头像">
                    <img  v-else :src=img>
                </div>
                <div class="info">
                    <div class="info_name">
                        <span > {{item.param.groupInfo.groupName}} </span>
                        &nbsp;
                        <span class="agreetxt" v-if="item.addWording != null">附言：{{  item.addWording  }}</span>
                    </div>
                    <div style="margin-top: 8px;">
                        <span v-if="userStore.userInfo.userId == item.memberId && item.operatorId != item.memberId">
                            <el-button color="#8a98c9" @click="showMemberInfo(item.operatorId)" round style="color: white;">{{ item.param.inviterInfo.nickName }}</el-button>
                            邀请您加入群聊
                        </span>
                        <span v-else-if="userStore.userInfo.userId == item.operatorId && item.operatorId != item.memberId">
                            您邀请
                            <el-button color="#8a98c9" @click="showMemberInfo(item.operatorId)" round style="color: white;">{{ item.param.inviterInfo.nickName }}</el-button>
                            加入群聊
                        </span>
                        <span v-else-if="item.operatorId == item.memberId">
                            <el-button color="#8a98c9" @click="showMemberInfo(item.memberId)" round style="color: white;">{{ item.param.memberInfo.nickName }}</el-button>
                            申请入群
                        </span>
                        <span v-else>
                            <el-button color="#8a98c9" @click="showMemberInfo(item.operatorId)" round style="color: white;">{{ item.param.inviterInfo.nickName }}</el-button>
                            邀请
                            <el-button color="#8a98c9" @click="showMemberInfo(item.memberId)" round style="color: white;">{{ item.param.memberInfo.nickName }}</el-button>
                            入群
                        </span>
                        &nbsp;
                        <span class="agreetxt">{{  item.createTime  }}</span>
                    </div>
                </div>

                <div class="requestBtnBox">
                    <div v-if="item.approveStatus == 0 && userStore.userInfo.userId != item.operatorId && userStore.userInfo.userId != item.memberId">
                        <el-button  class="request_btn" type="primary" @click="agreeRequest(item.id,1)">同意</el-button>
                        <el-button  class="request_btn" @click="RefuseRequest(item.id,2)">拒绝</el-button>
                    </div>
                    <div class="agreetxt" v-else-if="item.approveStatus == 0">等待管理员审批</div>

                    <div class="agreetxt" v-if="item.approveStatus == 1">已通过</div>
                    <div class="agreetxt" v-if="item.approveStatus == 2">已拒绝</div> 
                </div>

            </div>
        </div>

    </div>
</template>
<script setup>
import useSwitchStore from "../../../store/modules/switch";
import viewInfoStore from "../../../store/modules/viewInfo"
import useUserStore from '../../../store/modules/user'
import img from '../../../assets/img/profile.jpg'
import { ad } from '../../../api/group/group'
import { applyList ,approveApply} from '../../../api/group/apply'
import moadl from '../../../plugins/modal'
import { ref, reactive } from 'vue'
import {getRelationWithInfo} from "../../../api/friends/friend";


const userStore = useUserStore()
const switchStore = useSwitchStore()
const viewStore = viewInfoStore()
const groupRequestReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    operator: userStore.userInfo.userId
}) 
const dataList = ref([])

const isAdd = ref([])
let addFriendData = reactive({
    // UserGuId: userGuid,
    // FriendsGuId: "",
    // FriendsNote: null,
    // isAgree: null,
    // Reply: null,
})

let addGroupData = reactive({
    // GroupManagerGuId: userGuid,
    // UserGuId: "",
    // isAgree: null,
    // Reply: null,
})

// 查询数据 (组群申请)
async function getList() {
    await applyList(groupRequestReq.value).then((res) => {
        if (res.code == 200) {
            dataList.value = res.data
            // toList.value = res.data.toList
            console.log(dataList.value)
            // console.log(fromList.value)

        }
    })
}
getList();

let uid = ref()

let approveApplyReq = ref({
    id: null,
    status: null,
    operator: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})
// 同意申请
async function agreeRequest(id,status) {
    approveApplyReq.value.id = id
    approveApplyReq.value.status = status
    console.log("approveApplyReq.value = ",approveApplyReq.value)
    await approveApply(approveApplyReq.value).then((res) => {
        if (res.code == 200) {
            moadl.msgSuccess('申请已通过!')
            getList()
        }else{
            moadl.msgError(res.msg)
            getList()
        }
    })
}

// 拒绝申请
async function RefuseRequest(id,status) {
    approveApplyReq.value.id = id
    approveApplyReq.value.status = status
    console.log("approveApplyReq.value = ",approveApplyReq.value)
    await approveApply(approveApplyReq.value).then((res) => {
        if (res.code == 200) {
            moadl.msgSuccess('申请已拒绝')
            getList()
        }
    })
}

async function showMemberInfo(memberId){
    var getRelationReq = {}
    getRelationReq.appId = userStore.userInfo.appId
    getRelationReq.fromId = userStore.userInfo.userId
    getRelationReq.toId = memberId
    if(memberId == userStore.userInfo.userId){
        switchStore.isGroupChat = false
        switchStore.isChat = false
        switchStore.isFriendIntro = false
        switchStore.isFriends = false
        switchStore.isGroups = false
        switchStore.isVideo = false
        switchStore.isPersonal = true
    }else{
        getRelationWithInfo(getRelationReq).then((res)=>{
            if(res.code == 200){
                getMemberInfo(res.data,res.data.isFriend)
            }
        })
    }
}

async function getMemberInfo(v ,isFriend){
    console.log("isFriend = ",isFriend)
   if(isFriend == 1){
        var friendData = {...v.friendShip}
        friendData.param = {}
        friendData.param.userInfo = {...v}
        console.log("friendData = ",friendData)
        switchStore.FriendIntroData = friendData
        showFriendIntro(friendData)
    }else {
        var friendData = {}
        friendData.black = 0
        friendData.status = 0
        friendData.param = {}
        friendData.param.userInfo = {...v}
        console.log("friendData = ",friendData)
        switchStore.FriendIntroData = friendData
        showFriendIntro(friendData)
    }
    
    
}


async function showFriendIntro(v) {
    // console.log(v);
    switchStore.isFriendIntro = true
    switchStore.isGroupIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isChat = false
    switchStore.isVideo = false
    switchStore.isGroupChat = false
    switchStore.FriendIntroData = v
}

function back() {
    switchStore.isPersonal = false
    switchStore.isGroups = false
}
</script>

<style lang="scss" scoped>
.container1 {
    width: 100%;
    height: 100%;
    animation: ani 1s;
}

@keyframes ani {
    0% {
        transform: translateX(600px);
    }

    100% {
        transform: translateX(0px);
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

.backer {
    font-size: 1.2rem;
    display: flex;
    align-items: center;
    cursor: pointer;
}

.Box {
    width: 100%;
    height: 90%;
    overflow: hidden;
    overflow-y: scroll;
    // background-color: red;
    // background: rgb(red, green, blue);
}
.Box::-webkit-scrollbar { width: 0; height: 0; color: transparent; }

.requestBox {
    width: 100%;
    height: 90px;
    // background-color: red;
    display: flex;
    align-items: center;
    border-bottom: rgba(121, 163, 159, 0.3) 1px solid;
    position: relative;
}
.el-overlay{
     background-color: rgba(247, 247, 247, 0.2)!important;
     backdrop-filter: blur(10px) !important;
}
.imgbox {
    display: flex;
    margin: 0 15px;
    align-items: center;
}

.imgbox img {
    width: 70px;
    height: 70px;
    border-radius: 10px;
    object-fit: cover;
}

.info {
    font-size: 15px;
}

.requestBtnBox {
    position: absolute;
    right: 1rem;
}

.request_btn {
    width: 80px;
    height: 45px;
}
.agreetxt{
    font-size: 13px!important;
    color: #79a39f;
}
</style>