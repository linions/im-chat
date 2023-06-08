<template>
    <nav class="shell">
        <ul class="buttons">
            <li class="li">
                <div class="head" @click="clickPersonal">
                    <img :src="userInfo.photo"  alt="">
                </div>
            </li>
            
            <li class="li" @click="clickFriends()">
                <div class="tool" style="margin-top: 10px;">
                    <div v-if=" isReadValue == false" class="point"></div>
                    <img src="../../assets/img/addfirend.png" alt="">
                    <div class="point" v-if="!viewStore.isFriendShipRead"></div>
                </div>
            </li>

            <li class="li" @click="clickGroups()">
                <div class="tool" style="margin-top: 100px;">
                    <div v-if=" isReadValue == false" class="point"></div>
                    <img src="../../assets/img/addgroup.png" alt="">
                    <div class="point" v-if="!viewStore.isGroupRead"></div>
                </div>
            </li>

            <li class="li" @click="clickNotify()">
                <div class="set" style="margin-bottom: 160px;">
                    <div v-if=" isReadValue == false" class="point"></div>
                    <img src="../../assets/img/notify.png" alt="">
                    <div class="point" v-if="!viewStore.isNotifyRead"></div>
                </div>
            </li>

            <li class="li">
                <el-popover
                    popper-style="background-color: rgb(186 194 222);font-size: 20px;text-align: center"
                    ref="popover"
                    placement="right"
                    :width="200"
                    trigger="click"
                    content="this is content, this is content, this is content">
                    <template #reference>
                        <div class="set" style="margin-bottom: 80px;">
                            <img  src="../../assets/img/setting.png" alt="">
                        </div>
                    </template>
                    <el-row @click="changePass" style="border-bottom: dashed;" class="setting">
                        <el-col :span="24" >
                            修改密码
                        </el-col>
                    </el-row>
                    <el-row @click="findPass" style="border-bottom: dashed;" class="setting">
                        <el-col :span="24" >
                            找回密码
                        </el-col>
                    </el-row >
                    <el-popconfirm
                        width="220"
                        confirm-button-text="确认"
                        cancel-button-text="取消"
                        @confirm="changeBindMobile"
                        :icon="InfoFilled"
                        icon-color="#626AEF"
                        :title="bindText">
                        <template #reference>
                            <el-row class="setting" @click="bindMobile">
                                <el-col :span="24" >
                                    绑定手机号
                                </el-col>
                            </el-row>
                        </template>
                    </el-popconfirm>

                    <el-popconfirm
                        width="220"
                        confirm-button-text="确认"
                        cancel-button-text="取消"
                        @confirm="changeBindEmail"
                        :icon="InfoFilled"
                        icon-color="#626AEF"
                        :title="bindText">
                        <template #reference>
                            <el-row style="border-top: dashed;" class="setting" @click="bindEmail">
                                <el-col :span="24" >
                                    绑定邮箱
                                </el-col>
                            </el-row>
                        </template>
                    </el-popconfirm>

                    <el-popconfirm
                        width="220"
                        confirm-button-text="确认"
                        cancel-button-text="取消"
                        @confirm="deleteUser"
                        :icon="InfoFilled"
                        icon-color="#626AEF"
                        :title="bindText">
                        <template #reference>
                            <el-row style="border-top: dashed;" class="setting" @click="bindText = '此操作将注销您的账号，请您谨慎操作。'">
                                <el-col :span="24" >
                                    注销账号
                                </el-col>
                            </el-row>
                        </template>
                    </el-popconfirm>
                    
                    
                </el-popover>
                
            </li>            

            <li class="li" @click="userLoginOut">
                <div class="set">
                    <img src="../../assets/img/loginout.png" alt="">
                </div>
            </li>
        </ul>

        <!-- 修改密码 -->
        <el-dialog v-model="centerDialogVisible" title="修改密码" width="30%" class="el-diagLog" center>
            <el-row>
                <el-col :span="22">
                    <el-form-item :label-width="120" label="新密码：">
                        <el-input v-model="password" type="password" placeholder="请输入密码" show-password/>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item :label-width="120" label="确认密码：" prop="">
                        <el-input v-model="passwordNew" type="password"  placeholder="请输入密码" show-password/>
                    </el-form-item>
                </el-col>
            </el-row>

            <el-row>
                <el-col :span="16">
                    <el-form-item :label-width="120" label="验证码：" prop="">
                        <el-input v-model="code"  placeholder="请输入验证码" show-password/>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item :label-width="20" prop="">
                        <el-button v-if="canGet" @click="getVerCode()" style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                        <el-button v-else disabled style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                    </el-form-item>
                </el-col>
            </el-row>

            <template #footer>
            <span  class="dialog-footer">
                <el-button style="background-color: #8a98c9; color: white;" @click="cancelChangePassword()">取消</el-button>
                <el-button style="background-color: #8a98c9;" type="primary" @click="changePassword()"> 确认 </el-button>
            </span>
            </template>
        </el-dialog>

            <!-- 更换绑定认证 -->
            <el-dialog v-model="changeBindCertify" :title="changeBindText" width="30%" class="el-diagLog" center>
                <el-row>
                    <el-col :span="16">
                        <el-form-item :label-width="120" label="验证码：" prop="">
                            <el-input v-model="code"  placeholder="请输入验证码" show-password/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item :label-width="20" prop="">
                            <el-button v-if="canGet" @click="getVerCode()" style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                            <el-button v-else disabled style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                        </el-form-item>
                    </el-col>
                </el-row>

                <template #footer>
                <span  class="dialog-footer">
                    <el-button style="background-color: #8a98c9; color: white;" @click="cancelCertify()">取消</el-button>
                    <el-button style="background-color: #8a98c9;" type="primary" @click="confirmCertify()"> 确认 </el-button>
                </span>
                </template>
            </el-dialog>

        <!-- 更换手机号/邮箱绑定 -->
        <el-dialog v-model="isChangeBind" :title="changeBindText" width="30%" class="el-diagLog" center>
            <el-row v-if="type == 2">
                <el-col :span="22">
                    <el-form-item :label-width="120" label="更改手机号：">
                        <el-input v-model="mobile"  placeholder="请输入手机号" show-password/>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row v-if="type == 3"> 
                <el-col :span="22">
                    <el-form-item :label-width="120" label="更改邮箱：">
                        <el-input v-model="email"  placeholder="请输入邮箱" show-password/>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="16">
                    <el-form-item :label-width="120" label="验证码：" prop="">
                        <el-input v-model="code"  placeholder="请输入验证码" show-password/>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item :label-width="20" prop="">
                        <el-button v-if="canGet" @click="getNewCode()" style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                        <el-button v-else disabled style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                    </el-form-item>
                </el-col>
            </el-row>

            <template #footer>
            <span  class="dialog-footer">
                <el-button style="background-color: #8a98c9; color: white;" @click="cancelChangeBind()">取消</el-button>
                <el-button style="background-color: #8a98c9;" type="primary" @click="confirmChangeBind()"> 确认 </el-button>
            </span>
            </template>
        </el-dialog>


        <!-- 找回密码 -->
        <el-dialog v-model="findPassView" title="找回密码" width="30%" class="el-diagLog" center>
            <el-row>
                <el-col :span="16">
                    <el-form-item :label-width="120" label="验证码：" prop="">
                        <el-input v-model="code"  placeholder="请输入验证码" show-password/>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item :label-width="20" prop="">
                        <el-button v-if="canGet" @click="getVerCode()" style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                        <el-button v-else disabled style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                    </el-form-item>
                </el-col>
            </el-row>

            <template #footer>
            <span  class="dialog-footer">
                <el-button style="background-color: #8a98c9; color: white;" @click="cancelFindPassword()">取消</el-button>
                <el-button style="background-color: #8a98c9;" type="primary" @click="findUserPassword()"> 确认 </el-button>
            </span>
            </template>
        </el-dialog>

        <!-- 找回密码通知 -->
        <el-dialog v-model="findPassSuccessView" title="密码找回" width="30%" class="el-diagLog" center>
            <el-row style="margin-left: 20px; font-size: 18px; ">
                <el-col :span="24">
                    <el-form-item  label="您的账号为：" prop="">
                        <span style=" font-size: 18px;color: rgb(138, 152, 201);">
                            {{findPassSuccessResq.userId}}
                        </span>
                    </el-form-item>
                </el-col>
                <el-col :span="24">
                    <el-form-item  label="您绑定的手机号为：" prop="">
                        <span style=" font-size: 18px;color: rgb(138, 152, 201);">
                            {{findPassSuccessResq.mobile == null || findPassSuccessResq.mobile == '' ? "您还未绑定手机号" : findPassSuccessResq.mobile }}
                        </span>
                    </el-form-item>
                </el-col>
                <el-col :span="24">
                    <el-form-item  label="您绑定的邮箱为：" prop="">
                        <span style=" font-size: 18px;color: rgb(138, 152, 201);">
                            {{findPassSuccessResq.email == null || findPassSuccessResq.email == '' ? "您还未绑定邮箱" : findPassSuccessResq.email }}
                        </span>
                    </el-form-item>
                </el-col>
                <el-col :span="24">
                    <el-form-item  label="您的密码为：" prop="">
                        <span style=" font-size: 18px;color: rgb(138, 152, 201);">
                            {{findPassSuccessResq.password }}
                        </span>
                    </el-form-item>
                </el-col>
            </el-row>

            <template #footer>
            <span  class="dialog-footer">
                <el-button style="background-color: #8a98c9;" type="primary" @click="findPassSuccessView = false"> 确认 </el-button>
            </span>
            </template>
        </el-dialog>

        <!-- 确认认证方式 -->
        <el-dialog v-model="selectType" title="认证方式" width="30%" center>
            <el-row  >
                <el-col class="type1" @click="mobilePasChange" :span="12" style="border: solid rgb(138, 152, 201); height: 60px; line-height: 60px; text-align: center;
                                    font-size: 25px; background-color: white; color: rgb(138, 152, 201); border-radius: 10px 0px 0px 10px;">
                    手机号
                </el-col>
                <el-col class="type2" @click="emailPasChange" :span="12" style="height: 60px; line-height: 60px; text-align: center; font-size: 25px;
                                    background-color: rgb(138, 152, 201); color: white; border-radius: 0px 10px 10px 0px">
                    邮箱
                </el-col>
            </el-row>
        </el-dialog>


        <!-- 注销账号 -->
        <el-dialog v-model="deleteUserView" title="注销账号" width="30%" class="el-diagLog" center>
            <el-row>
                <el-col :span="16">
                    <el-form-item :label-width="120" label="验证码：" prop="">
                        <el-input v-model="code"  placeholder="请输入验证码" show-password/>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item :label-width="20" prop="">
                        <el-button v-if="canGet" @click="getVerCode()" style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                        <el-button v-else disabled style="background-color: #8a98c9;" type="primary" round>{{btnText}}</el-button>
                    </el-form-item>
                </el-col>
            </el-row>

            <template #footer>
            <span  class="dialog-footer">
                <el-button style="background-color: #8a98c9; color: white;" @click="cancelDeleteUser()">取消</el-button>
                <el-button style="background-color: #8a98c9;" type="primary" @click="deleteUserConfirm()"> 确认 </el-button>
            </span>
            </template>
        </el-dialog>
    </nav>

</template>

<script setup>
import { ref, reactive, watch,nextTick} from 'vue';
import useUserStore from '../../store/modules/user';
import useSwitchStore from "../../store/modules/switch";
import { applyList, checkApply, read } from '../../api/friends/apply'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'
import Cookies from 'js-cookie'
import { updateChatUser,getCode ,logout} from '../../api/login'
import { findPassword ,getCertify,destroyUser} from '../../api/user/user'
import useViewStore from '../../store/modules/viewInfo';

const router = useRouter()
const userStore = useUserStore()
const switchStore = useSwitchStore()
const viewStore = useViewStore()
let userInfo = reactive({...userStore.userInfo})

const readFriendShipRequestReq = reactive({
    fromId: userStore.userInfo.userId,
    appId : userStore.userInfo.appId,
})

const isChangeBind = ref(false)
const bindText = ref()

const changeBindText = ref()
const findPassView = ref(false)
const findPassSuccessView = ref(false)
const deleteUserView = ref(false)
const findPassSuccessResq = ref({})
const isFindPass = ref(false)
const isChangePass = ref(false)
const isDeleteUser = ref(false)
const selectType = ref(false)
const centerDialogVisible = ref(false)
const changeBindCertify = ref(false)
const password = ref()
const passwordNew = ref()
const code = ref()
const type = ref()
const mobile = ref()
const email = ref()
const btnText = ref("获取验证码")
var countdown = 60
const canGet = ref(true)

const getCodeReq = ref({
    appId:userStore.userInfo.appId,
    mobile:userStore.userInfo.mobile,
    email:userStore.userInfo.email,
    type:2,
})

const certifyReq = ref({
    appId:userStore.userInfo.appId,
    account:null,
    code:null,
})

const findPasswordReq = ref({
    userId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    mobile:userStore.userInfo.mobile,
    email:userStore.userInfo.email,
    type:2,
    code:null,
})

const passwordChangeReq = ref({
    userId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    password :null,
    type:2,
    code:null,
})

const changBindReq = ref({
    userId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    email :null,
    type:2,
    mobile:null,
    code:null,
})

const changeBindReq = ref({
    userId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    email :null,
    mobile:null,
    type:null,
    code:null,
})

const userReq = ref({
    appId:userStore.userInfo.appId,
    userId:userStore.userInfo.userId
})

function clickPersonal() {
    switchStore.isGroupChat = false
    switchStore.isChat = false
    switchStore.isFriendIntro = false
    switchStore.isFriends = false
    switchStore.isGroups = false
    switchStore.isPersonal = true
    switchStore.isNotification = false

}

function clickFriends() {
    switchStore.isGroupChat = false
    switchStore.isChat = false
    switchStore.isFriendIntro = false
    switchStore.isGroupIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = true
    switchStore.isGroups = false
    switchStore.isNotification = false

    viewStore.isFriendShipRead = true
}

function clickGroups() {
    switchStore.isGroupChat = false
    switchStore.isChat = false
    switchStore.isFriendIntro = false
    switchStore.isGroupIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isGroups = true
    switchStore.isNotification = false

    viewStore.isGroupRead = true

}

function clickNotify() {
    switchStore.isGroupChat = false
    switchStore.isChat = false
    switchStore.isFriendIntro = false
    switchStore.isGroupIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isGroups = false
    switchStore.isNotification = true

    viewStore.isNotifyRead = true

}


let isReadValue = ref(true);


async function cancelChangeBind(){
    isChangeBind.value = false
    code.value = null
    if(type.value == 2){
        mobile.value = null
    }
    if(type.value == 3){
        email.value = null
    }
}

async function confirmChangeBind(){
    changBindReq.value.code = code.value
    changBindReq.value.type = type.value
    if(type.value == 2){
        changBindReq.value.mobile = mobile.value
    }
    if(type.value == 3){
        changBindReq.value.email = email.value
    }
    updateChatUser(changBindReq.value).then((res) => {
        if (res.code == 200) {
            if(changBindReq.value.type == 2){
                ElMessage.success('已更换手机号绑定！')
            }
            if(changBindReq.value.type == 3){
                ElMessage.success('已更换邮箱绑定！')
            }
            cancelChangeBind()
        }else{
            ElMessage.error('密码修改失败！')
        }
    })
}


function cancelCertify(){
    changeBindCertify.value = false
    code.value = null
    certifyReq.value.account = null
}

function cancelDeleteUser(){
    deleteUserView.value = false
    code.value = null
    if(type.value == 2){
        mobile.value = null
    }
    if(type.value == 3){
        email.value = null
    }
}

async function deleteUserConfirm(){
    deleteUserReq.value.code = code.value
    await destroyUser(deleteUserReq.value).then((res) => {
        if (res.code == 200) {
            isDeleteUser.value = false
            deleteUserView.value = false
            code.value = null
            ElMessage.success('账号注销成功！')
            countdown = 0
            userLoginOut(loginOutReq)
        }else{
            ElMessage.error('账号注销失败！')
        }
    })
}

async function confirmCertify(){
    certifyReq.value.code = code.value
    await getCertify(certifyReq.value).then((res) => {
        if (res.code == 200) {
            changeBindCertify.value = false
            isChangeBind.value = true
            code.value = null
            if(type.value == 2){
                changeBindText.value = "更换手机号绑定"
            }else{
                changeBindText.value = "更换邮箱绑定"
            }
            ElMessage.success('认证成功！')
            countdown = 0
        }else{
            ElMessage.error('认证失败！')
        }
    })
}

function changeBindMobile(){
    certifyReq.value.account = userStore.userInfo.mobile
    type.value = 2 
    changeBindText.value = "原手机号认证"
    changeBindCertify.value = true
}

function changeBindEmail(){
    certifyReq.value.account = userStore.userInfo.email
    if(certifyReq.value.account == null || certifyReq.value.account == ''){
        isChangeBind.value = true
    }else{
        changeBindText.value = "原邮箱认证"
        changeBindCertify.value = true
    }
    type.value = 3 
    
}

function bindMobile(){
    if(userStore.userInfo.mobile == null || userStore.userInfo.mobile == undefined || userStore.userInfo.mobile == ''){
        bindText.value = "您还未绑定手机号，请按确认进行绑定"
    }else{
        bindText.value = "您已绑定手机号，请按确认进行更换绑定"
    }
    getCodeReq.value.type = 2
}

function bindEmail(){
    if(userStore.userInfo.email == null || userStore.userInfo.email == undefined || userStore.userInfo.email == ''){
        bindText.value = "您还未绑定邮箱，请按确认进行绑定"
    }else{
        bindText.value = "您已绑定邮箱，请按确认进行更换绑定"
    }
    getCodeReq.value.type = 3
}

function deleteUser(){
    selectType.value = true
    isDeleteUser.value = true
}

function changePass(){
    selectType.value = true
    isChangePass.value = true
    isFindPass.value = false
}

function findPass(){
    selectType.value = true
    isChangePass.value = false
    isFindPass.value = true
}

watch(() => userStore.userInfo, async (value) => {
    userInfo = userStore.userInfo
})

async function findUserPassword(){

    findPasswordReq.value.code = code
    await findPassword(findPasswordReq.value).then((res) => {
        if (res.code == 200) {
            findPassSuccessResq.value = res.data
            findPassSuccessView.value = true
            code.value = null
            findPassView.value = false
        }else{
            ElMessage.error('密码找回失败！')
        }
    })
}


const deleteUserReq = ref({
    appId:userStore.userInfo.appId,
    userId:userStore.userInfo.userId,
    code:null,
    mobile:userStore.userInfo.mobile,
    email:userStore.userInfo.email,
    type:2    
})

function mobilePasChange(){
   
    if(userStore.userInfo.mobile == null){
        ElMessage.error("您还未绑定手机号")
    }else{
        getCodeReq.value.type = 2
        selectType.value = false
        if(isChangePass.value){
        passwordChangeReq.value.type = 2
        centerDialogVisible.value = true
        findPassView.value = false
        deleteUserView.value = false
        }
        if(isFindPass.value){
            findPasswordReq.value.type = 2
            centerDialogVisible.value = false
            findPassView.value = true
            deleteUserView.value = false

        }
        if(isDeleteUser.value){
            deleteUserReq.value.type = 2
            centerDialogVisible.value = false
            findPassView.value = false
            deleteUserView.value = true
        }
    }
    

}

function emailPasChange(){
    if(userStore.userInfo.email == null){
        ElMessage.error("您还未绑定邮箱")
    }else{
        getCodeReq.value.type = 3
        passwordChangeReq.value.type = 3
        findPasswordReq.value.type = 3
        deleteUserReq.value.type = 3
        selectType.value = false
        if(isChangePass.value){
            centerDialogVisible.value = true
            findPassView.value = false
            deleteUserView.value = false
        }
        if(isFindPass.value){
            centerDialogVisible.value = false
            findPassView.value = true
            deleteUserView.value = false
        }
        if(isDeleteUser.value){
            centerDialogVisible.value = false
            findPassView.value = false
            deleteUserView.value = true
        }
    }
   
}

function cancelFindPassword(){
    code.value = null
    findPassView.value = false
}

function cancelChangePassword(){
    password.value = null
    passwordNew.value = null
    code.value = null
    centerDialogVisible.value = false
}

async function changePassword (){
    passwordChangeReq.value.code = code.value
    if(password.value != passwordNew.value ){
        ElMessage.error('密码不一致')
        return
    }

    var reg = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,}$/
    if (!reg.test(password.value)) {
        ElMessage.error('密码必须包含字母和数字，且长度不低于8位。')
        return
    }

    passwordChangeReq.value.password = password.value
    console.log("passwordChangeReq = ",passwordChangeReq.value)

    updateChatUser(passwordChangeReq.value).then((res) => {
        if (res.code == 200) {
            ElMessage.success('密码修改成功！')
            userStore.getUserInfo(userReq.value);
            password.value = null
            passwordNew.value = null
            code.value = null
            centerDialogVisible.value = false
        }else{
            ElMessage.error('密码修改失败！')
        }
    })
    
}

async function getNewCode(){
    console.log("getCodeReq = ",getCodeReq.value)
    if(mobile.value != null){
        getCodeReq.value.mobile = mobile.value
    }else if(getCodeReq.value.type == 2){
        ElMessage.warning("手机号不能为空！")
    }
    if(email.value != null){
        getCodeReq.value.email = email.value
    }else if(getCodeReq.value.type == 3){
        ElMessage.warning("邮箱不能为空！")
    }

    await getCode(getCodeReq.value).then((res) => {
        if (res.code == 200) {
        canGet.value = false
        ElMessage.success("验证码已发送！")
        settime()  
        }else{
            if(type.value == 2){
                ElMessage.error("手机认证失败，请先进行绑定")
            }else{
                ElMessage.error("邮箱认证失败，请先进行绑定")
            }
        }
    })
}

async function getVerCode(){
    console.log("getCodeReq = ",getCodeReq.value)
    await getCode(getCodeReq.value).then((res) => {
        if (res.code == 200) {
        canGet.value = false
        ElMessage.success("验证码已发送！")
        settime()  
        }else{
            if(type.value == 2){
                ElMessage.error("手机认证失败，请先进行绑定")
            }else{
                ElMessage.error("邮箱认证失败，请先进行绑定")
            }
        }
    })
}

function settime() {
    if (countdown == 0) {
        btnText.value = "获取验证码";
        countdown = 60;  
        canGet.value = true       
        console.log("countdown = ",countdown)

        return ;
    } else {
        btnText.value ="重新发送(" + countdown + ")";
        countdown--;
        console.log("countdown = ",countdown)
    }
    setTimeout(function() {   //设置一个定时器，每秒刷新一次
        settime();
    },1000);
}

async function handleRead() {
    const { code, data, msg } = await read(readFriendShipRequestReq);
    if (code == 200) {
    }
}

const loginOutReq = ref({
    appId:userStore.userInfo.appId,
    userId:userStore.userInfo.userId,
})

function userLoginOut() {
    logout(loginOutReq.value).then((res)=>{
        ElMessage.success("登出成功！")
        router.push({
            path: "/login"
        })
        Cookies.remove('userId')
        localStorage.clear()
        setTimeout(() => {
            location.reload()
        }, 500);
    })

  
}

</script>

<style scoped>
.shell {
    position: relative;
    width: 90px;
    height: 700px;
    border-top-left-radius: 10px;
    border-bottom-left-radius: 10px;
    background-color: #8a98c9;
}

.buttons {
    color: #fff;
}

.head {
    position: absolute;
    left: 20%;
    top: 2%;
    cursor: pointer;
}

.head img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 50%;
}



.type1:hover{
    background-color: rgb(138, 152, 201)!important;
    color: white!important;
    border: white solid 3px;
}

.type2:hover{
    color: rgb(138, 152, 201)!important;
    background-color: white!important;
    border: #8a98c9 solid 3px;
    border-left: none;
}


.tool {
    position: absolute;
    left: 33%;
    top: 20%;
}

.tool img,
.set img {
    width: 33px;
    cursor: pointer;
}

.setting{
    height: 40px;
    width: 100%;
    text-align: 40px;
    line-height: 40px;
}

.setting :hover{
    color: white;
}

.point {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 12px;
    height: 12px;
    background-color: red;
    border-radius: 50%;
    z-index: 99;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 5px;
    color: #fff;
}

.set {
    position: absolute;
    left: 30%;
    bottom: 2%;
}

.li {
    letter-spacing: 2px;
    font: 600 14px '';
    transition: .3s;
    list-style: none;
}
</style>
