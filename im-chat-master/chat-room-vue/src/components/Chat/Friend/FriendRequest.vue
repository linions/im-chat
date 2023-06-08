<template>
    <div class="container1">
        <div class="topbar">
            <div>好友申请</div>
            <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>
        </div>

        <div class="Box">
            <div class="requestBox" v-for="(item,index) in dataList" :key="index" >
                <div class="imgbox" @click="showFriendIntro(item.param.userInfo)">
                    <img v-if="item.param.userInfo.photo != null && item.param.userInfo.photo != ''" :src=item.param.userInfo.photo alt="头像">
                    <img  v-else :src=img>
                </div>
                <div class="info" @click="showFriendIntro(item.param.userInfo)">
                    <div class="info_name">
                        <span v-if="(item.toId == userStore.userInfo.userId)"> {{item.fromId}} </span>
                        <span v-if="(item.fromId == userStore.userInfo.userId)"> {{item.toId}} </span>
                        &nbsp;
                        <span class="agreetxt" v-if="item.addWording != null">附言：{{  item.addWording  }}</span>
                    </div>
                    <div style="margin-top: 8px;">
                        <span v-if="(item.toId == userStore.userInfo.userId)">申请添加你为好友</span>
                        <span v-if="(item.fromId == userStore.userInfo.userId)">请求添加对方为好友</span>
                        &nbsp;
                        <span class="agreetxt">{{  item.createTime  }}</span>
                    </div>
                </div>

                <div class="requestBtnBox">
                    <div v-if="item.toId == userStore.userInfo.userId && item.approveStatus == 0">
                        <el-button  class="request_btn" type="primary" @click="agreeRequest(item.id,1)">同意</el-button>
                        <el-button  class="request_btn" @click="RefuseRequest(item.id,2)">拒绝</el-button>
                    </div>
                    <div class="agreetxt" v-if="item.fromId == userStore.userInfo.userId && item.approveStatus == 0">等待验证</div>
                    <div class="agreetxt" v-if="item.approveStatus == 1">已通过</div>
                    <div class="agreetxt" v-if="item.approveStatus == 2">已拒绝</div>
                </div>

            </div>
        </div>

    </div>
</template>

<script setup>
import useSwitchStore from "../../../store/modules/switch"
import viewInfoStore from "../../../store/modules/viewInfo"
import useUserStore from '../../../store/modules/user'
import img from '../../../assets/img/profile.jpg'
import { applyList,approveApply } from '../../../api/friends/apply'
import moadl from '../../../plugins/modal'
import { ref, reactive,watch } from 'vue'
import { getRelation } from '../../../api/friends/friend'

const userStore = useUserStore()
const switchStore = useSwitchStore()
const viewStore = viewInfoStore()
let userId = userStore.userInfo.userId
let appId = userStore.userInfo.appId

const dataList = ref([])

const friendShipRequestReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
}) 

const getRelationReq = ref({
    fromId: userId,
    appId: userStore.userInfo.appId,
    toId : null
}) 

let approveApplyReq = ref({
    id: null,
    status: null,
    operator: userId,
    appId: appId
})

// 好友简介
async function showFriendIntro(friendInfo) {
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isChat = false
    switchStore.isGroupChat = false
    getRelationReq.value.toId = friendInfo.userId
    await getRelation(getRelationReq.value).then((res) => {
        if (res.code == 200) {
            var friendData = res.data
            friendData.param.userInfo = friendInfo
            switchStore.FriendIntroData = friendData
            switchStore.isFriendIntro = true

            console.log("friendData = ",friendData)

        }else{
            var friendData = {}
            friendData.black = 0
            friendData.status = 0
            friendData.param = {}
            friendData.param.userInfo = friendInfo
            console.log("friendData = ",friendData)
            switchStore.FriendIntroData = friendData
            switchStore.isFriendIntro = true

        }
    })
}


// 查询数据 (好友申请)
async function getList() {
    await applyList(friendShipRequestReq.value).then((res) => {
        if (res.code == 200) {
            dataList.value = res.data
            // console.log("dataList.value = " , dataList.value)
        }
    })
}

const allFriendShipReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

// 同意申请 添加好友
async function agreeRequest(id,status) {
    approveApplyReq.value.id = id
    approveApplyReq.value.status = status
    console.log("approveApplyReq.value = ",approveApplyReq.value)
    await approveApply(approveApplyReq.value).then((res) => {
        if (res.code == 200) {
            moadl.msgSuccess('添加成功!')
            // viewStore.getFriendList(allFriendShipReq.value)
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
            moadl.msgSuccess('已拒绝对方的请求')
            getList()
        }
    })
}
getList();

// watch(() => dataList.value, async (value) => {
//         await getList()
// })



function back() {
    switchStore.isPersonal = false
    switchStore.isFriends = false
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
    margin-left: 10px;
    font-size: 13px!important;
    color: #79a39f;
}
</style>