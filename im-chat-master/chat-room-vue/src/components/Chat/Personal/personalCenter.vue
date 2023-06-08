<template>
    <div class="container">
        <div class="topbar">
            <div>个人中心</div>
            <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>
        </div>

        <div class="mt20 personal">
            <el-form ref="formRef" :model="formData.value" :rules="rules" >
                <el-row>
                    <UploadImage3 ref="uploadRef" @modelValue="getUploadInfo" v-if="isUpload" v-model="formData.photo" :limit="1" :fileSize="5" :drag="true" />
                </el-row>

                <el-row>
                    <el-col :span="24">
                        <el-form-item :label-width="labelWidth" label="个性签名">
                            <el-input
                                v-if="isUpdate"
                                style="font-size: 20px;"
                                type="textarea"
                                :rows="2"
                                placeholder="请编辑个性签名"
                                v-model="formData.value.selfSignature">
                            </el-input>
                            <span v-else style="font-size: 20px; color:#8a98c9">{{ userInfo.selfSignature  }} </span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="账号">
                            <span style="font-size: 20px; color:#8a98c9">{{ userInfo.userId }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="昵称" prop="nickName">
                            <el-input v-if="isUpdate" style="font-size: 20px;" type="text" v-model="formData.value.nickName" placeholder="请输入昵称"></el-input>
                            <span v-else style="font-size: 20px; color:#8a98c9" >{{ userInfo.nickName }}</span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="生日">
                            <div class="block" v-if="isUpdate">
                                <el-date-picker
                                size="large"
                                v-model="formData.value.birthDay"
                                type="date"
                                placeholder="选择日期"
                                format="YYYY-MM-DD"
                                value-format="YYYY-MM-DD">
                                </el-date-picker>
                            </div>
                            <span  v-else style="font-size: 20px; color:#8a98c9">{{ userInfo.birthDay }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="性别">
                            <span v-if="userInfo.userSex == 0 && !isUpdate" style="font-size: 20px; color:#8a98c9"> 未设置 </span>
                            <span v-if="userInfo.userSex == 1 && !isUpdate" style="font-size: 20px; color:#8a98c9"> 男 </span>
                            <span v-if="userInfo.userSex == 2 && !isUpdate" style="font-size: 20px; color:#8a98c9"> 女 </span>

                            <el-radio-group v-if="isUpdate" v-model="formData.value.userSex" class="ml-4" >
                                <el-radio :label="1" border>男</el-radio>
                                <el-radio :label="2" border>女</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="电话">
                            <span style="font-size: 20px; color:#8a98c9">{{ userInfo.mobile }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="邮箱" prop="">
                            <span style="font-size: 20px; color:#8a98c9">{{ userInfo.email }}</span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="好友验证">
                            <el-select v-model="formData.value.friendAllowType" placeholder="请选择" v-if="isUpdate">
                                <el-option
                                v-for="item in friendAllowTypeList"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                                >
                                </el-option>
                            </el-select>
                            <span v-else-if="userInfo.friendAllowType == 1" style="font-size: 20px; color:#8a98c9">无需验证</span>
                            <span v-else-if="userInfo.friendAllowType == 2" style="font-size: 20px; color:#8a98c9">开启验证</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="类型" prop="">
                            <span style="font-size: 20px; color:#8a98c9">{{ userInfo.userType == 1 ? "普通用户" : userInfo.userType == 2 ? "客服" : "机器人" }}</span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="24">
                        <el-form-item :label-width="labelWidth" label="地区">
                            <el-cascader v-if="isUpdate"
                            size="large"
                            :props="{ checkStrictly: true }"
                            :options="locationList"
                            v-model="formData.value.location"
                            @change="handleChange">
                            </el-cascader>
                            <span v-else-if="userInfo.location != null && userInfo.location != ''" style="font-size: 20px; color:#8a98c9">{{ userInfo.location  }} </span>
                            <span v-else style="font-size: 20px; color:#8a98c9"> 未知 </span>
                        </el-form-item>
                    </el-col>
                </el-row>
                
            </el-form>
            <el-row>
                <el-col :span="24" style="display: flex;justify-content: center;">
                    <el-button v-if="!isUpdate" @click="handleEdit" class="button">编辑
                    </el-button>
                    <el-button v-else @click="handleCancel" class="button" >返回
                    </el-button>
                    <el-button v-if="isUpdate" @click="handleSubmit(formData)" class="button">保存
                    </el-button>
                </el-col>
            </el-row>

        </div>

    </div>
</template>

<script setup>
import { reactive, ref,nextTick } from "vue";
import useUserStore from "../../../store/modules/user";
import useSwitchStore from "../../../store/modules/switch";
import modal from "../../../plugins/modal";
import { updateChatUser } from '../../../api/login'
import { regionData, CodeToText, TextToCode } from 'element-china-area-data'


const userStore = useUserStore()
const switchStore = useSwitchStore()
const userInfo = userStore.userInfo

const formData = ref({ ...userInfo });

const userReq = ref({
    appId:userStore.userInfo.appId,
    userId:userStore.userInfo.userId
})

const friendAllowTypeList = ref([
    {value:1,label:'无需验证'},
    {value:2,label:'开启验证'}
])

const locationList = regionData

const formRef = ref();
const labelWidth = 100;
const isUpdate = ref(false)
const isUpload = ref(true)


const rules = reactive({
    nickName: [
        {
            required: true,
            message: "请输入昵称",
        },
    ],
})

function handleChange(locationData){
    console.log("locationData = ",locationData)
    // getCodeToText(null,locationData)
}

function handleCancel(){
    formData.value=  ref({...userInfo})
    // console.log("formData.value = ",formData.value)
    isUpdate.value = false
    // getCodeToText(null,locationData)
}

function handleEdit(){
    formData.value=  ref({...userInfo})
    isUpdate.value = true
    formData.value.value.location = getTextToCode(formData.value.value.location,null)
}

function getCodeToText (codeStr, codeArray) {
    if (null === codeStr && null === codeArray) {
    return null;
    } else if (null === codeArray) {
    codeArray = codeStr.split(",");
    }
    let area = "";
    switch (codeArray.length) {
    case 1:
        area += CodeToText[codeArray[0]];
        break;
    case 2:
        area += CodeToText[codeArray[0]] + "/" + CodeToText[codeArray[1]];
        break;
    case 3:
        area +=
        CodeToText[codeArray[0]] +
        "/" +
        CodeToText[codeArray[1]] +
        "/" +
        CodeToText[codeArray[2]];
        break;
    default:
        break;
    }
    return area;
}

function getTextToCode (codeStr, codeArray) {
    if (null === codeStr && null === codeArray) {
        return null;
    } else if (null === codeArray) {
        codeArray = codeStr.split("/");
        console.log(codeArray)
    }
    let code = [];
    switch (codeArray.length) {
    case 1:
        code[0] = TextToCode[codeArray[0]].code;
        break;
    case 2:
        code[0] = TextToCode[codeArray[0]].code;
        code[1] = TextToCode[codeArray[0]][codeArray[1]].code;
        break;
    case 3:
        console.log(TextToCode[codeArray[0]])

        code[0] = TextToCode[codeArray[0]].code;
        code[1] = TextToCode[codeArray[0]][codeArray[1]].code;
        code[2] = TextToCode[codeArray[0]][codeArray[1]][codeArray[2]].code;
        break;
    default:
        break;
    }
    return code;
}

const handleSubmit = async (formData) => {
    formData.value.location = getCodeToText(null,formData.value.location)
    formData.value.roleId = userStore.userInfo.role.roleId
    console.log("formData = ",formData)
    if (!formData.value.nickName) {
        modal.msgError('昵称不能为空！')
        return
    }
    formData.value.mobile = null
    formData.value.email = null
    const { code } = await updateChatUser(formData.value);
    if (code == 200) {
        modal.msgSuccess('用户信息修改成功！')
    }
    await userStore.getUserInfo(userReq.value);
    formData.value = ref({...userStore.userInfo})
    setup()
    console.log("formData.value = ",formData.value)
    isUpdate.value = false
}


function back() {
    switchStore.isChat = false
    switchStore.isFriendIntro = false
    switchStore.isPersonal = false
    switchStore.isGroupChat = false
    switchStore.isFriends = false
}

function setup(){
    //使用刷新
    switchStore.isPersonal = false
    nextTick(()=>{
        //写入操作
        switchStore.isPersonal = true
    })
}

</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

.container {
    width: 100%;
    height: 100%;
    animation: ani 1s;
    box-shadow: 1px 2px 5px rgba(0, 0, 0, 0.4);
}

@keyframes ani {
    0% {
        transform: translateX(600px);
    }

    100% {
        transform: translateX(0px);
    }
}
.dialog-footer button:first-child {
  margin-right: 10px;
}

.button{
    width: 80px;
    height:40px;
    font-size: 20px;
    border-radius: 10px;
    background-color: #8a98c9;
    color: white;
}

.el-diagLog el-dialog{
    border-radius: 20px;
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