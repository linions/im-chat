<template>
    <div class="container1">
        <div class="topbar">
            <div>系统通知</div>
            <el-button @click="isUserReport = true" class="button"> 用户反馈</el-button>
            <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>
        </div>

        <div class="Box">
            <div class="requestBox" v-for="(item,index) in dataList" :key="index" >
                <div class="imgbox" >
                    <img v-if="item.type == 1" src="../../assets/img/notification.png" alt="头像">
                    <img  v-else src="../../assets/img/notice.png">
                </div>
                <div class="info">
                    <div class="info_name">
                        <span class="agreetxt"> {{item.title}} </span>
                        &nbsp;
                        <span  v-if="item.type == 2 && item.fromId != null && item.fromId == userStore.userInfo.userId">来自您的用户反馈</span>
                        <span  v-else-if="item.type == 2 && item.toId != null && item.toId == userStore.userInfo.userId">给与您的用户反馈</span>
                        <span  v-else-if="item.type == 1">来自系统通知</span>
                    </div>
                    <div style="margin-top: 8px;width:500px;">
                        <span style="color: #d13838"  v-if="item.type == 2 && item.toId != null && item.toId == userStore.userInfo.userId">
                            您已被其他用户举报，请注意您的言行举止，文明交流！否则，情形严重者，经审查后将会被封号处理！ 
                        </span>
                        <span  v-else-if="item.type == 2 && item.toId != null && item.fromId == userStore.userInfo.userId">
                            被举报人: 
                           <span class="agreetxt">{{ item.toId }}</span>
                        </span>
                        <span  v-else-if="item.content != null">
                           <span style="color: rgb(122 142 214)">{{ item.content }}</span>
                        </span>

                    </div>
                    <div style="color: #79a39f; margin-top: 8px; width:500px;" v-if="item.type == 2 && item.feedBack != null">
                            处理反馈: 
                           <span style="color: black">{{ item.feedBack }}</span>
                        </div>
                    <span class="agreetxt" style="float: right; margin-right: 30px;">{{  item.createTime  }}</span>
                </div>

                <div class="requestBtnBox" v-if="item.type == 2">
                    <div class="agreetxt" v-if="item.status == 1">已处理</div>
                    <div class="agreetxt" v-else>等待处理</div>  
                </div>

            </div>
        </div>


         <!-- 用户反馈 -->
         <el-dialog v-model="isUserReport" title="用户反馈" width="20%">
            <div style="margin-bottom: 10px;">
                主题:<el-input  v-model="createNotifyReq.title" placeholder="请输入主题"></el-input>
            </div>
            <div style="margin-bottom: 10px;">
                反馈内容:
                <el-input type="textarea"
                :autosize="{ minRows: 4, maxRows: 8}"
                placeholder="请输入反馈内容"
                v-model="createNotifyReq.content">
                </el-input>
            </div>
           
        
            <template #footer>
                <span class="dialog-footer">
                    <el-button style="background-color: #8a98c9; color: white;" @click="isUserReport = false">取消</el-button>
                    <el-button style="background-color: #8a98c9;" type="primary" @click="confirmReport()"> 确认 </el-button>
                </span>
            </template>
        </el-dialog>

    </div>
</template>
<script setup>
import useSwitchStore from "../../store/modules/switch";
import viewInfoStore from "../../store/modules/viewInfo"
import useUserStore from '../../store/modules/user'
import img from '../../assets/img/profile.jpg'
import { ad } from '../../api/group/group'
import { applyList ,approveApply} from '../../api/group/apply'
import moadl from '../../plugins/modal'
import { ref, reactive } from 'vue'
import {getRelationWithInfo} from "../../api/friends/friend";
import {getNotify,sendNotify} from "../../api/notify";
import { ElMessage, ElMessageBox } from 'element-plus';


const userStore = useUserStore()
const switchStore = useSwitchStore()
const viewStore = viewInfoStore()
const notifyReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
}) 
const dataList = ref([])
const isUserReport = ref(false)

const createNotifyReq = ref({
    fromId:userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    title:null,
    content:null,
    type:2,
})


//用户反馈
async function confirmReport() {
    sendNotify(createNotifyReq.value).then((res)=>{
        if(res.code == 200){
            ElMessage.success("您的反馈提交成功，请耐心等待处理")
            getList()
        }else{
            ElMessage.error("反馈失败，请联系管理员进行处理")
        }
    })
    isUserReport.value = false
}

// 查询数据 (组群申请)
async function getList() {
    await getNotify(notifyReq.value).then((res) => {
        if (res.code == 200) {
            dataList.value = res.data
            console.log(dataList.value)
        }
    })
}
getList();






function back() {
    switchStore.isNotification = false
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
}
.Box::-webkit-scrollbar { width: 0; height: 0; color: transparent; }

.button{
    font-size: 20px;
    border-radius: 10px;
    background-color: #8a98c9;
    color: white;
}
.requestBox {
    width: 100%;
    height: 120px;
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