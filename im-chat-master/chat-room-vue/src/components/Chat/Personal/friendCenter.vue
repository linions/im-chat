<template>
    <div class="container">
        <div class="topbar">
            <div>好友简介</div>
            <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>
        </div>
        <el-icon @click="reportUser" style="float: right;margin: 10px;position: relative;" size="25px"><WarningFilled color="red"/></el-icon>

        <div class="mt20 personal">
            <el-form ref="formRef" :model="formData.value">
                <el-row>
                    <el-col :span="24" style="display: flex;justify-content: center;margin: 15px 0 10px 0;">
                        <img class="message_img" style="margin: 0 20px;"  v-if="formData.photo == null" src="../../../assets/img/profile.jpg" alt="">
                        <img class="message_img" v-else style="margin: 0 20px;"   :src="formData.photo"  alt="">     
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="24">
                        <el-form-item>
                            <el-input v-if="isUpdate" style="font-size: 20px;"  type="text" v-model="updateReq.toItem.remark" placeholder="请输入备注" class="nickNameInput"></el-input>
                            <span v-else-if="FriendIntroData.remark !=null && FriendIntroData.remark !=''" class="nickName">{{ FriendIntroData.remark  }} </span>
                            <span v-else class="nickName">{{ formData.nickName }} </span>

                            <i v-if="!isUpdate && FriendIntroData.remark !=null && FriendIntroData.remark !=''" @click="isUpdate = true"  class="fa fa-pencil-square-o" style="font-size: 20px; cursor: pointer; padding: 10px;"></i>
                           
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" style="display: flex;justify-content: center;">
                        <el-button v-if="isUpdate" style="background-color: #8a98c9;" @click="handleCancel" type="primary" round>取消</el-button>
                        <el-button v-if="isUpdate" style="background-color: #8a98c9;" @click="handleInputConfirm" type="primary" round>确认</el-button>
                    </el-col>
                            
                </el-row>
                

                <el-row>
                    <el-col :span="24">
                        <el-form-item :label-width="labelWidth" label="个性签名">
                            <span style="font-size: 20px; color:#8a98c9">{{ formData.selfSignature  }} </span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="账号">
                            <span style="font-size: 20px; color:#8a98c9">{{ formData.userId }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="昵称" prop="nickName">
                            <span style="font-size: 20px; color:#8a98c9" >{{ formData.nickName }}</span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="生日">
                            <span style="font-size: 20px; color:#8a98c9">{{ formData.birthDay }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="性别">
                            <span v-if="formData.userSex == 0 && !isUpdate" style="font-size: 20px; color:#8a98c9"> 未设置 </span>
                            <span v-if="formData.userSex == 1 && !isUpdate" style="font-size: 20px; color:#8a98c9"> 男 </span>
                            <span v-if="formData.userSex == 2 && !isUpdate" style="font-size: 20px; color:#8a98c9"> 女 </span>
                        </el-form-item>
                    </el-col>
                </el-row>
                
                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="邮箱" prop="">
                            <span  style="font-size: 20px; color:#8a98c9">{{ formData.mobile }}</span>
                        </el-form-item>
                    </el-col>

                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="好友关系" prop="">
                            <span v-if="FriendIntroData.black == 0" style="font-size: 20px; color:#8a98c9"> 未添加 </span>
                            <span v-else-if="FriendIntroData.black == 1" style="font-size: 20px; color:#8a98c9"> 正常 </span>
                            <span v-else style="font-size: 20px; color:#8a98c9"> 已拉黑 </span>
                        
                            <el-button v-if="FriendIntroData.black == 1 || FriendIntroData.black == 0" style="background-color: #8a98c9; margin-left: 30px;" type="primary" round @click="addBlack()">拉黑</el-button>
                            <el-button v-else  style="background-color: #8a98c9; margin-left: 30px;" type="primary" round @click="removeBlack()">正常</el-button>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="24">
                        <el-form-item :label-width="labelWidth" label="地区">
                            <span v-if="formData.location != null && formData.location != ''" style="font-size: 20px; color:#8a98c9">{{ formData.location  }} </span>
                            <span v-else style="font-size: 20px; color:#8a98c9"> 未知 </span>
                        </el-form-item>
                    </el-col>
                </el-row>

            </el-form>

            <el-row v-if="FriendIntroData.black != 2">
                <el-col :span="24" style="display: flex;justify-content: center; margin: 15px 0">
                    <el-button v-if="FriendIntroData.status == 1" style="margin-right: 50px" @click="removeFriend()" class="button">删除好友</el-button>
                    <el-button v-else style="margin-right: 50px" @click="addButton(formData.userId)" class="button">添加好友</el-button>
                    <el-button v-if="FriendIntroData.status == 1" @click="sentMessage()" class="button"> 发消息</el-button>
                </el-col>
            </el-row>          
        </div>


        <el-dialog v-model="isAdd" title="添加好友" width="20%">
            <div style="margin-bottom: 10px;">
                备注:<el-input  v-model="applyFriendData.toItem.remark" placeholder="请输入备注"></el-input>
            </div>
            <div style="margin-bottom: 10px;">
                附言:<el-input  v-model="applyFriendData.toItem.addWording" placeholder="请输入附言"></el-input>
            </div>
            <div style="margin-bottom: 10px;">
                添加来源:
                <el-select v-model="applyFriendData.toItem.addSource" disabled placeholder="添加来源">
                    <el-option
                    v-for="item in addSource"
                    :key="item.value"
                    :label="item.label"
                    :value="item.label">
                    </el-option>
                </el-select>
            </div>
        
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="cancelAdd">取消</el-button>
                    <el-button type="primary" @click="handleApply">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <el-dialog v-model="isReportUser" title="举报用户" width="20%">
            <div style="margin-bottom: 10px;">
                原因:
                <el-input type="textarea"
                :autosize="{ minRows: 4, maxRows: 8}"
                placeholder="请输入举报原因"
                v-model="notifyReq.content">
                </el-input>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="isReportUser = false">取消</el-button>
                    <el-button type="primary" @click="confirmReport">确定</el-button>
                </span>
            </template>
        </el-dialog>

            

    </div>
</template>

<script setup>
import {ref} from "vue";
import useUserStore from "../../../store/modules/user";
import useSwitchStore from "../../../store/modules/switch";
import modal from "../../../plugins/modal";
import { updateNote ,blackFriend ,cancelBlackFriend,deleteFriend} from '../../../api/friends/friend'
import useChatStore from "../../../store/modules/chat"
import viewInfoStore from '../../../store/modules/viewInfo'
import { ElMessage, ElMessageBox } from 'element-plus';
import { getConversation} from '../../../api/conversation/conversation'
import { sendApply } from '../../../api/friends/apply'
import { sendNotify } from '../../../api/notify'


const userStore = useUserStore()
const switchStore = useSwitchStore()
const chatStore = useChatStore()
const viewStore = viewInfoStore()

const FriendIntroData = switchStore.FriendIntroData
console.log("FriendIntroData = ",FriendIntroData)

const friendInfo = FriendIntroData.param.userInfo
const formData = ref({ ...friendInfo });
console.log("formData = ",formData.value)

const formRef = ref();
const labelWidth = 100;

const isUpdate = ref(false)

let getChatReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

const allFriendShipReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

function back() {
    switchStore.isChat = false
    switchStore.isGroupChat = false
    switchStore.isPersonal = false
    switchStore.isFriendIntro = false
    switchStore.isGroups = false
    switchStore.isFriends = false
}

const updateReq = ref({
    appId: userStore.userInfo.appId,
    fromId: userStore.userInfo.userId,
    toItem:{
        toId: friendInfo.userId,
        remark: FriendIntroData.remark
    }
})

const blackReq = ref({
    appId: userStore.userInfo.appId,
    fromId: userStore.userInfo.userId,
    toId: friendInfo.userId,
})

const addSource = ref([
    {value:1,label:'好友查找'},
    {value:2,label:'好友推荐'},
    {value:3,label:'组群信息'},
    {value:4,label:'组群查找'},
    {value:5,label:'好友邀请'},
    {value:6,label:'好友查找'},
])
let isAdd = ref(false);
const applyFriendData = ref({
    appId:userStore.userInfo.appId,
    fromId: userStore.userInfo.userId,
    toItem:{
        toId : null,
        remark : null,
        addSource: null,
        addWording: null,
    }
});

function addButton(toId) {
    console.log(toId)
    isAdd.value = true;
    applyFriendData.value.toItem.toId = toId;
    applyFriendData.value.toItem.addSource = addSource.value[0].label
}

function cancelAdd(){
    isAdd.value = false
    applyFriendData.value.toItem.toId = null
    applyFriendData.value.toItem.remark = null
    applyFriendData.value.toItem.addSource = null
    applyFriendData.value.toItem.addWording = null
}


// 提交申请 (添加好友)
async function handleApply() {
    console.log("applyFriendData.value = " ,applyFriendData.value)
    const { code, data, msg } = await sendApply(applyFriendData.value);
    if (code == 200) {
        ElMessage.success("添加好友请求成功！")
        viewStore.getFriendList(allFriendShipReq.value)
        cancelAdd()
    }
    else {
        modal.msgError(msg)
        cancelAdd()
    }
}


const isReportUser = ref(false)
const notifyReq = ref({
    fromId:userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    title:"用户举报",
    content:null,
    type:2,
})
// 举报用户
async function reportUser() {
    ElMessageBox.confirm('此操作将执行举报该用户操作, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        isReportUser.value = true
    }).catch(() => {
        isReportUser.value = false
        modal.msgWarning("操作已取消")
    });
}

// 举报用户
async function confirmReport() {
    notifyReq.value.toId = switchStore.FriendIntroData.toId,
    sendNotify(notifyReq.value).then((res)=>{
        if(res.code == 200){
            ElMessage.success("举报成功")
        }else{
            ElMessage.error("举报失败，请联系管理员处理")
        }
    })
    notifyReq.value.content = null
    isReportUser.value = false
}




// 显示标签输入
async function handleCancel() {
    isUpdate.value = false
    updateReq.value.toItem.remark = FriendIntroData.remark
}

// 标签确认
async function handleInputConfirm() {
    await updateNote(updateReq.value).then((res) => {
        if (res.code == 200) {
            setTimeout(() => {
                FriendIntroData.remark = updateReq.value.toItem.remark
                viewStore.getConversationList(getChatReq.value)
                modal.msgSuccess("好友备注修改成功!")
            }, 500);
        }
    })
    isUpdate.value = false
}


function addBlack(){
    ElMessageBox.confirm('此操作将执行拉黑该好友操作, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
            blackFriend(blackReq.value).then((res) => {
                if (res.code == 200) {
                    setTimeout(() => {
                        FriendIntroData.black = 2
                        modal.msgSuccess("拉黑好友成功!")
                    }, 500);
                }
            })
        }).catch(() => {
            modal.msgWarning("操作已取消")
        });
}

function removeBlack(){
    ElMessageBox.confirm('此操作将执行取消拉黑该好友操作, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
            cancelBlackFriend(blackReq.value).then((res) => {
                if (res.code == 200) {
                    setTimeout(() => {
                        FriendIntroData.black = 1
                        modal.msgSuccess("取消拉黑好友成功!")
                    }, 500);
                }
            })
        }).catch(() => {
            modal.msgWarning("操作已取消")
        });
}


function removeFriend(){
    ElMessageBox.confirm('此操作将执行删除该好友操作, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
            deleteFriend(blackReq.value).then((res) => {
                if (res.code == 200) {
                    setTimeout(() => {
                        FriendIntroData.status = 2
                        viewStore.getFriendList(allFriendShipReq.value)
                        modal.msgSuccess("删除好友成功!")
                    }, 500);
                }
            })
        }).catch(() => {
            modal.msgWarning("操作已取消")
        });
}

let getConversationReq = ref({
    fromId: userStore.userInfo.userId,
    toId : "",
    groupId : "",
    appId: userStore.userInfo.appId,
    conversationType: null
})


function sentMessage(){
    switchStore.isFriendIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isGroupChat = false
    switchStore.isGroups = false
    chatStore.chatUser = friendInfo
    console.log("chatStore.chatUser = ",chatStore.chatUser)

    getConversationReq.value.toId = friendInfo.userId
    getConversationReq.value.conversationType = 0
    console.log("getConversationReq.value = ",getConversationReq.value)
    getConversation(getConversationReq.value).then((res) => {
        if (res.code == 200) {
            chatStore.friendShip = res.data[0].friendShip
            console.log(" chatStore.friendShip = ", chatStore.friendShip)
            switchStore.isChat = true
            viewStore.getConversationList(getChatReq.value)
        }
    })
}


</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

.container {
    width: 100%;
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

.message_img {
    width: 150px !important;
    height: 150px !important;
    background-color: #fff;
    border-radius: 50%;
    overflow: hidden;
    object-fit: cover;
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

.ml15 {
    margin-left: 15px;
}

.mt20 {
    margin-top: 20px;
}
.nickName{
    width: 150px;
    font-size: 20px;
    color: #e1e3eb;
    margin-left: 250px;
    text-align: center;
    height: auto;
    background-color: #8a98c9;
    border: solid #cacaca;
    border-radius: 15px;
}

.nickNameInput{
    width: 250px;
    font-size: 20px;
    color: #e1e3eb;
    margin-left: 200px;
    height: auto;
}

.topbar {
    width: 95%;
    height: 70px;
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

.personal {
    margin-right: 30px;
}
</style>