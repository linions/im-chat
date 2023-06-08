<template>
    <div class="container">
        <div class="topbar">
            <div>组群简介</div>
            <div @click="back" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>
        </div>

        <div class="mt20 personal">
            <el-form ref="formRef" :model="GroupIntroData">
                <div class="common-layout">
                    <el-container>
                    <el-aside width="200px" height="200px">
                        <UploadImage v-if="isUpdate" @photo="photo" :isDisabled = "false" style="float: left;margin: 0 20px;" ref="uploadRef" v-model="GroupIntroData.photo" :limit="1" :fileSize="5" :drag="true" />
                        <div v-else>
                            <img class="message_img" style="margin: 0 20px;"  v-if="GroupIntroData.photo == null" src="../../../assets/img/profile.jpg" alt="">
                            <img class="message_img" v-else style="margin: 0 20px;"   :src="GroupIntroData.photo"  alt="">         
                        </div>
                    </el-aside>
                    <el-container>
                        <el-header style="margin: 20px 0 0 0;height: 20px;">
                            <el-form-item label="群昵称" v-if="isUpdate">
                                <el-input  style="font-size: 20px;"  type="text" v-model="updateReq.groupName" placeholder="请输入群昵称" class="nickNameInput"></el-input>
                            </el-form-item>
                            <span v-else class="nickName">{{ GroupIntroData.groupName }} </span> 

                        </el-header>
                        <el-main>
                            <el-form-item label="群简介">
                                <el-input v-if="isUpdate" type="textarea"
                                style="float: left;width: 450px;"
                                :autosize="{ minRows: 4, maxRows: 8}"
                                placeholder="请输入群介绍"
                                v-model="updateReq.introduction">
                                </el-input>
                                <span v-else style="font-size: 20px; color:#8a98c9">{{GroupIntroData.introduction}} </span>
                            </el-form-item>
                            
                        </el-main>
                    </el-container>
                    </el-container>
                </div>
                <!-- <el-row>
                    <el-col :span="24" style="display: flex;justify-content: center;margin: 15px 0 10px 0;">
                        <UploadImage :isDisabled="true" ref="uploadRef" v-model="GroupIntroData.photo" :limit="1" :fileSize="5" :drag="true" />
                    </el-col>
                </el-row> -->
                <el-row style="margin-top: 20px;">
                    <el-col :span="24">
                        <el-form-item :label-width="labelWidth" label="群公告">
                            <el-input v-if="isUpdate" type="textarea"
                            style="float: left;width: 450px;"
                            :autosize="{ minRows: 2, maxRows: 4}"
                            placeholder="请输入群公告"
                            v-model="updateReq.notification">
                            </el-input>
                            <span v-else style="font-size: 20px; color:#8a98c9">{{ GroupIntroData.notification  }} </span>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="群号">
                            <span style="font-size: 20px; color:#8a98c9">{{ GroupIntroData.groupId }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="群类型" prop="nickName">
                            <!-- <el-select v-if="isUpdate" v-model="updateReq.groupType" placeholder="群聊类型">
                                <el-option
                                style="padding-left: 20px;"
                                v-for="item in groupTypeList"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                                </el-option>
                            </el-select> -->
                            <div >
                                <span v-if="GroupIntroData.groupType == 1" style="font-size: 20px; color:#8a98c9">私有群</span>
                                <span v-else style="font-size: 20px; color:#8a98c9">公开群</span>
                            </div>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="禁言状态">
                            <el-select v-if="isUpdate" v-model="updateReq.mute" placeholder="禁言类型">
                                <el-option
                                style="padding-left: 20px;"
                                v-for="item in muteTypeList"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                                </el-option>
                            </el-select>
                            <div v-else>
                                <span v-if="GroupIntroData.mute == 0" style="font-size: 20px; color:#8a98c9">不禁言</span>
                                <span v-else style="font-size: 20px; color:#8a98c9">全员禁言</span>
                            </div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="申请入群">
                            <el-select v-if="isUpdate" v-model="updateReq.applyJoinType" placeholder="申请加群">
                                <el-option
                                style="padding-left: 20px;"
                                v-for="item in applyJoinTypeList"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                                </el-option>
                            </el-select>
                            <div v-else>
                                <span v-if="GroupIntroData.applyJoinType == 0" style="font-size: 20px; color:#8a98c9">禁止任何人申请加入</span>
                                <span v-if="GroupIntroData.applyJoinType == 1" style="font-size: 20px; color:#8a98c9">群主或管理员审批</span>
                                <span v-if="GroupIntroData.applyJoinType == 2" style="font-size: 20px; color:#8a98c9">自由加入群组</span>
                            </div>
                        </el-form-item>
                    </el-col>
                </el-row>
                
                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="最大成员数">
                            <el-select v-if="isUpdate" v-model="updateReq.maxMemberCount"  placeholder="群聊人数">
                                <el-option
                                style="padding-left: 20px;"
                                v-for="item in maxMemberList"
                                :key="item.value"
                                :label="item.value"
                                :value="item.value">
                                </el-option>
                            </el-select>
                            <span v-else style="font-size: 20px; color:#8a98c9">{{ GroupIntroData.maxMemberCount}}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="群成员">
                            <span style="font-size: 20px; color:#8a98c9">{{ GroupIntroData.memberList.length }}</span>

                            <el-popover placement="right" :width="400" trigger="click">
                                <template #reference>
                                    <el-button style="background-color: #8a98c9; margin-left: 30px;" type="primary" round>查看</el-button>
                                </template>
                               
                                
                                <div class="demo-collapse">
                                    <el-collapse accordion>
                                    <el-collapse-item name="1" style="margin-bottom: 0!import;">
                                        <template #title > 管理员 </template>

                                        <el-popover
                                        v-for="(item,index) in GroupIntroData.memberList" :key="index"
                                        popper-style="background-color: rgb(186 194 222);font-size: 16px;text-align: center"
                                        ref="popover"
                                        placement="left"
                                        :width="30"
                                        trigger="click"
                                        content="this is content, this is content, this is content">
                                        <template #reference>

                                            <div  class="historyBox" v-if="item.memberId != userStore.userInfo.userId">
                                                <div v-if="item.role == 1 || item.role == 2" style="height: 60px;border-bottom: dashed 2px rgb(121, 163, 159);background-color:  #b4cee9;border-radius: 10px; padding: 10px 0 10px 0;">
                                                    <div >
                                                        <span>
                                                            <img @click="memberInfo" class="imgbox" :src="item.photo" alt="头像">
                                                        </span>
                                                        <span class="content">
                                                            <span class="_message_content_box"  >
                                                                    {{item.alias}}
                                                            </span>
                                                            <span>
                                                                    {{item.joinTime}}
                                                            </span>
                                                        </span>
                                                        <button v-if="item.isFriend == 0"  class="add">非好友</button>
                                                        <button v-else-if ="item.mute == 1"  class="add">禁言中...</button>

                                                        <span style="float: right;">{{item.role == 2 ? '群主' : '管理员'}}</span>
                                                    </div>
                                                </div>
                                            </div>
                                            <div  class="historyBox" v-else>
                                                <div v-if="item.role == 1 || item.role == 2" style="height: 60px;border-bottom: dashed 2px rgb(121, 163, 159);background-color:rgb(175, 187, 228); border-radius: 10px; padding: 10px 0 10px 0;">
                                                    <div >
                                                        <span>
                                                            <img @click="memberInfo" class="imgbox" :src="item.photo" alt="头像">
                                                        </span>
                                                        <span class="content">
                                                            <span class="_message_content_box"  >
                                                                    {{item.alias}}
                                                            </span>
                                                            <span>
                                                                    {{item.joinTime}}
                                                            </span>
                                                        </span>
                                                        <button v-if ="item.mute == 1"  class="add">禁言中...</button>

                                                        <span style="float: right;">我</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                        <el-row @click="showMemberInfo(item.memberId, item.isFriend)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                查看
                                            </el-col>
                                        </el-row>
                                        <el-row v-if="item.isFriend == 0 && item.memberId != userStore.userInfo.userId" @click="addFriend(item.memberId)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                加好友
                                            </el-col>
                                        </el-row>
                                        <el-row v-if="item.mute != 1 && isOwner && item.memberId != userStore.userInfo.userId" @click="muteMember(item.memberId)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                禁言
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="item.mute == 1 && isOwner && item.memberId != userStore.userInfo.userId" @click="disMuteMember(item.memberId,item.speakDate)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                解除禁言
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="isOwner && item.memberId != userStore.userInfo.userId" @click="setRole(item.memberId,0)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                设为普通成员
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="isOwner && item.memberId != userStore.userInfo.userId" @click="transferOwner(item.memberId)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                群主转移
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="isOwner && item.memberId != userStore.userInfo.userId" @click="removeMember(item.memberId) " style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                踢出群聊
                                            </el-col>
                                        </el-row >

                                
                                        
                                    </el-popover>

                                        


                                    </el-collapse-item>
                                    <el-collapse-item name="2">
                                        <template #title > 普通成员 </template>
                                       
                                        <el-popover
                                        v-for="(item,index) in GroupIntroData.memberList" :key="index"
                                        popper-style="background-color: rgb(186 194 222);font-size: 16px;text-align: center"
                                        ref="popover"
                                        placement="left"
                                        :width="30"
                                        trigger="click"
                                        content="this is content, this is content, this is content">
                                        <template #reference>

                                            <div  class="historyBox" v-if="item.memberId != userStore.userInfo.userId">
                                                <div v-if="item.role == 0" style="height: 60px;border-bottom: dashed 2px rgb(121, 163, 159);background-color:  #b4cee9;border-radius: 10px; padding: 10px 0 10px 0;">
                                                    <div >
                                                        <span>
                                                            <img @click="memberInfo" class="imgbox" :src="item.photo" alt="头像">
                                                        </span>
                                                        <span class="content">
                                                            <span class="_message_content_box"  >
                                                                    {{item.alias}}
                                                            </span>
                                                            <span>
                                                                    {{item.joinTime}}
                                                            </span>
                                                        </span>
                                                        <button v-if="item.isFriend == 0"  class="add">非好友</button>
                                                        <button v-else-if="item.mute == 1"  class="add">禁言中...</button>

                                                        <span style="float: right;">成员</span>
                                                    </div>
                                                </div>
                                            </div>
                                            <div  class="historyBox" v-else>
                                                <div v-if="item.role == 0" style="height: 60px;border-bottom: dashed 2px rgb(121, 163, 159);background-color:rgb(175, 187, 228); border-radius: 10px; padding: 10px 0 10px 0;">
                                                    <div >
                                                        <span>
                                                            <img @click="memberInfo" class="imgbox" :src="item.photo" alt="头像">
                                                        </span>
                                                        <span class="content">
                                                            <span class="_message_content_box"  >
                                                                    {{item.alias}}
                                                            </span>
                                                            <span>
                                                                    {{item.joinTime}}
                                                            </span>
                                                        </span>
                                                        <button v-if="item.mute == 1"  class="add">禁言中...</button>


                                                        <span style="float: right;">我</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                        <el-row @click="showMemberInfo(item.memberId, item.isFriend)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                查看
                                            </el-col>
                                        </el-row>
                                        <el-row v-if="item.isFriend == 0 && item.memberId != userStore.userInfo.userId" @click="addFriend(item.memberId)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                加好友
                                            </el-col>
                                        </el-row>
                                        <el-row v-if="item.mute != 1 && GroupIntroData.isManager && item.memberId != userStore.userInfo.userId" @click="muteMember(item.memberId)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                禁言
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="item.mute == 1 && GroupIntroData.isManager && item.memberId != userStore.userInfo.userId" @click="disMuteMember(item.memberId,item.speakDate)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                解除禁言
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="isOwner && item.memberId != userStore.userInfo.userId" @click="setRole(item.memberId,1)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                设为管理员
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="isOwner && item.memberId != userStore.userInfo.userId" @click="transferOwner(item.memberId)" style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                群主转移
                                            </el-col>
                                        </el-row >
                                        <el-row v-if="GroupIntroData.isManager && item.memberId != userStore.userInfo.userId" @click="removeMember(item.memberId) " style="border-bottom: dashed;" class="setting">
                                            <el-col :span="24" >
                                                踢出群聊
                                            </el-col>
                                        </el-row >

                                
                                        
                                    </el-popover>

                                    </el-collapse-item>

                                    </el-collapse>
                                </div>

                            </el-popover>

                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="群主">
                            <span style="font-size: 20px; color:#8a98c9">{{GroupIntroData.ownerId}} </span>
                        </el-form-item>
                    </el-col>
                    
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="我的群昵称">
                            <el-input v-if="isUpdateNickName" style="font-size: 20px;"  type="text" v-model="groupNickName" placeholder="请输入群昵称"></el-input>
                            <span v-else style="font-size: 20px; color:#8a98c9">{{ groupNickName }}</span>
                            <i v-if="!isUpdateNickName" @click="isUpdateNickName = true"  class="fa fa-pencil-square-o" style="font-size: 20px; cursor: pointer; margin-left:30px"></i>
                            <div v-if="isUpdateNickName" style="margin-left: 180px;">
                                <el-icon size="large" color="green" @click="UpdateNickName()"><CircleCheck /></el-icon>
                                <el-icon size="large" color="red" @click="cancelNickName()"><CircleClose /></el-icon>
                            </div>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="群状态">
                            <span v-if="GroupIntroData.status == 1" style="font-size: 20px; color:#8a98c9">正常</span>
                            <span v-else style="font-size: 20px; color:#8a98c9">群已解散</span>
                        </el-form-item>
                    </el-col>
                    
                    <el-col :span="12">
                        <el-form-item :label-width="labelWidth" label="邀请成员">
                            <el-select v-if="isAdd"
                                v-model="AddGroupMemberReq.members"
                                multiple
                                no-data-text="无可邀请好友"
                                collapse-tags
                                style="float: left;"
                                placeholder="请选择群成员"
                                >
                                <el-option
                                style="padding-left: 20px;"
                                v-for="item in MembersList"
                                :key="item.param.userInfo.userId"
                                :label="item.param.userInfo.nickName"
                                :value="item.param.userInfo.userId"
                                :disabled="item.disabled">
                                <span style="float: left">{{ item.param.userInfo.nickName }}</span>
                                <span style="float: left; padding-left: 10px; color: #8492a6; font-size: 13px">({{ item.param.userInfo.userId }})</span>  
                                </el-option>
                            </el-select>
                            <el-button v-if="!isAdd" style="background-color: #8a98c9; margin-left: 30px;" type="primary" round @click="isAdd=true">邀请</el-button>
                            <div v-if="isAdd" style="margin-left: 180px;">
                                <el-icon size="large" color="green" @click="addMembers()"><CircleCheck /></el-icon>
                                <el-icon size="large" color="red" @click="cancelAddMembers()"><CircleClose /></el-icon>
                            </div>
                        </el-form-item>
                    </el-col>
                   
                </el-row>

            </el-form>

             <el-row >
                <el-col :span="24" style="display: flex;justify-content: center; margin: 15px 0" v-if="GroupIntroData.isMember">
                    <div v-if="GroupIntroData.isManager && GroupIntroData.status == 1">
                        <el-button v-if="!isUpdate && isOwner" style="margin-right: 50px" @click="destroyGroupByOwner()" class="button">解散群聊</el-button>
                        <el-button v-if="!isUpdate && !isOwner" style="margin-right: 50px" @click="exitGroup()" class="button">退出群聊</el-button>
                        <el-button v-if="!isUpdate" style="margin-right: 50px" @click="isUpdate = true" class="button">编辑</el-button>
                        <el-button v-if="isUpdate" style="margin-right: 50px" @click="cancelEdit()" class="button">取消</el-button>
                        <el-button v-if="isUpdate" style="margin-right: 50px" @click="updateGroupInfo()" class="button">保存</el-button>
                    </div>
                    <div v-else-if="GroupIntroData.status == 1">
                        <el-button v-if="!isUpdate" style="margin-right: 50px" @click="exitGroup()" class="button">退出群聊</el-button>
                    </div>
                    <el-button v-if="!isUpdate && GroupIntroData.status == 1" @click="sentMessage()" class="button"> 发消息</el-button>

                    <!-- <el-button v-if="!isUpdate && (isOwner || GroupIntroData.isManager) " style="margin-right: 50px" @click="isUpdate = true" class="button">编辑</el-button>
                    <el-button v-if="isUpdate && (isOwner || GroupIntroData.isManager) " style="margin-right: 50px" @click="cancelEdit()" class="button">取消</el-button>
                    <el-button v-if="isUpdate && (isOwner || GroupIntroData.isManager) " style="margin-right: 50px" @click="updateGroupInfo()" class="button">保存</el-button> -->
                </el-col>
                <el-col :span="24" style="display: flex;justify-content: center; margin: 15px 0" v-else>
                    <el-button v-if="!isUpdate && GroupIntroData.status == 1" @click="isAddGroup=true" class="button"> 申请入群</el-button>
                </el-col>
            </el-row>      

            
        </div>

        <!-- 添加好友 -->
        <el-dialog v-model="isAddFriend" title="添加好友" width="20%">
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
                    <el-button style="background-color: #8a98c9; color: white;" @click="cancelAdd()">取消</el-button>
                    <el-button style="background-color: #8a98c9;" type="primary" @click="handleApply()"> 确认 </el-button>
                </span>
            </template>
        </el-dialog>


        <!-- 成员禁言 -->
        <el-dialog v-model="isMuteMember" title="禁言时间设置" width="20%">
            <div style="margin-bottom: 10px;">
                <div class="block">
                    <span class="demonstration">禁言时间：</span>
                    <el-date-picker
                        v-model="muteReq.speakDate"
                        type="datetime"
                        placeholder="Select date and time"
                    />
                </div>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button style="background-color: #8a98c9; color: white;" @click="cancelMute()">取消</el-button>
                    <el-button style="background-color: #8a98c9;" type="primary" @click="confirmMute()"> 确认 </el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 解除禁言 -->
        <el-dialog v-model="isDisMuteMember" title="解除禁言通知" width="20%">
            <div style="font-size: 15px;">
                该成员的禁言时间为：
                <div class="block" style="margin-bottom: 10px;margin-top: 10px;font-size: 18px;color: rgb(138, 152, 201);">
                    {{ muteReq.speakDate }}
                </div>
                您确认解除该成员的禁言时间？
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button style="background-color: #8a98c9; color: white;" @click="cancelDisMute()">取消</el-button>
                    <el-button style="background-color: #8a98c9;" type="primary" @click="confirmDisMute()"> 确认 </el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 解除禁言 -->
        <el-dialog v-model="isRemoveMember" title="踢出群聊" width="20%">
            <div style="font-size: 18px;color: rgb(138, 152, 201);">
                您确认要将该成员踢出群聊？
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button style="background-color: #8a98c9; color: white;" @click="cancelRemove()">取消</el-button>
                    <el-button style="background-color: #8a98c9;" type="primary" @click="confirmRemove()"> 确认 </el-button>
                </span>
            </template>
        </el-dialog>

<!-- 申请入群 -->
        <el-dialog v-model="isAddGroup" title="申请入群" width="20%">
            <div style="margin-bottom: 10px;">
                附言:<el-input  v-model="AddGroupMemberReq.addWording" placeholder="请输入附言"></el-input>
            </div>
            <div style="margin-bottom: 10px;">
                添加来源:
                <el-input  disabled="true" placeholder="组群号搜索"></el-input>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="isAddGroup = false">取消</el-button>
                    <el-button type="primary" @click="sendGroupApply()">确定</el-button>
                </span>
            </template>
        </el-dialog>

        


       

            

    </div>
</template>

<script setup>
import { reactive, ref, watch ,nextTick} from "vue";
import useUserStore from "../../../store/modules/user";
import useSwitchStore from "../../../store/modules/switch";
import modal from "../../../plugins/modal";
import useChatStore from "../../../store/modules/chat"
import viewInfoStore from '../../../store/modules/viewInfo'
import { ElMessage, ElMessageBox } from 'element-plus';
import { getConversation} from '../../../api/conversation/conversation'
import { sendApply } from '../../../api/friends/apply'
import { groupInfo,updateGroup,updateGroupMember,addGroupMember,muteGroupMember,exitGroupMember,transferGroup,destroyGroup,removeGroupMember} from '../../../api/group/group'
import {getRelationWithInfo} from "../../../api/friends/friend";




const maxMemberList = ref([
    {value:20,label:'最小群'},
    {value:50,label:'小群'},
    {value:75,label:'中小群'},
    {value:100,label:'中等群'},
    {value:200,label:'一般群'},
    {value:250,label:'正常群'},
    {value:300,label:'较大群'},
    {value:400,label:'大群'},
    {value:500,label:'最大群'},
])
const groupTypeList = ref([
    {value:1,label:'私有群'},
    {value:2,label:'公开群'}
])
const muteTypeList = ref([
    {value:0,label:'不禁言'},
    {value:1,label:'全员禁言'}
])

const applyJoinTypeList = ref([
    {value:0,label:'禁止任何人申请加入'},
    {value:1,label:'群主或管理员审批'},
    {value:2,label:'自由加入群组'}
])



const userStore = useUserStore()
const switchStore = useSwitchStore()
const chatStore = useChatStore()
const viewStore = viewInfoStore()

let members = []
for(let i = 0 ; i < viewStore.friendList.length ; i++){
    members[i] = viewStore.friendList[i].toId
}
console.log("members = ",members)


const MembersList = []
const MembersDataList = [...viewStore.friendList]
const GroupIntroData = ref({...switchStore.GroupIntroData})
console.log("GroupIntroData.value.memberList = ",GroupIntroData.value.memberList)
// console.log("MembersDataList = ",MembersDataList)
// console.log("GroupIntroData = ",GroupIntroData)
var count = 0
for(let i = 0 ; i < MembersDataList.length ; i++){
    let hasMember = false
    for(let j = 0 ; j < GroupIntroData.value.memberList.length; j++){
        if(MembersDataList[i].toId  == GroupIntroData.value.memberList[j].memberId){
            hasMember = true
        }
    }
    if(hasMember == false){
        MembersList[count] = MembersDataList[i]
        count++
    }
}

console.log("MembersList = ",MembersList)


const formRef = ref();
const labelWidth = 100;

const isOwner = ref(GroupIntroData.value.ownerId == userStore.userInfo.userId ? true :false)
const isUpdate = ref(false)
const isUpdateNickName = ref(false)

let groupNickName = ref(GroupIntroData.value.groupNickName == null && GroupIntroData.value.groupNickName == '' ? GroupIntroData.value.nickName : GroupIntroData.value.groupNickName)


const AddGroupMemberReq = ref({
    appId: switchStore.GroupIntroData.appId,
    groupId: switchStore.GroupIntroData.groupId,
    operator:userStore.userInfo.userId,
    members:[],
    isManager:GroupIntroData.value.isManager,
    clientType:1,
    addWording:null,
    isInvite:0,
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
    switchStore.isGroupIntro = false
    switchStore.isGroups = false
    switchStore.isFriends = false
    switchStore.isVideo = false
}



const updateReq = ref({
    appId: switchStore.GroupIntroData.appId,
    groupId: switchStore.GroupIntroData.groupId,
    operator:userStore.userInfo.userId,
    memberId:userStore.userInfo.userId,
    groupName: GroupIntroData.value.groupName,
    groupType:GroupIntroData.value.groupType,
    mute: GroupIntroData.value.mute,
    applyJoinType: GroupIntroData.value.applyJoinType,
    introduction:GroupIntroData.value.introduction,
    notification:GroupIntroData.value.notification,
    photo:GroupIntroData.value.photo,
    maxMemberCount:GroupIntroData.value.maxMemberCount,

})

const updateMemberReq = ref({
    appId: switchStore.GroupIntroData.appId,
    groupId: switchStore.GroupIntroData.groupId,
    operator:userStore.userInfo.userId,
    memberId:null,
    alias:null,
    role:null,
    mute:null,
})


let isAdd = ref(false);






function cancelEdit() {
    // console.log(toId)
    isUpdate.value = false;
    updateReq.value.groupName = GroupIntroData.value.groupName
    updateReq.value.groupType = GroupIntroData.value.groupType
    updateReq.value.mute = GroupIntroData.value.mute
    updateReq.value.applyJoinType = GroupIntroData.value.applyJoinType
    updateReq.value.introduction = GroupIntroData.value.introduction
    updateReq.value.notification = GroupIntroData.value.notification
    updateReq.value.photo = GroupIntroData.value.photo
    updateReq.value.maxMemberCount = GroupIntroData.value.maxMemberCount
}

function cancelAddMembers(){
    console.log("AddGroupMemberReq = ",AddGroupMemberReq.value)
    AddGroupMemberReq.value.members = []
    isAdd.value = false
}

const photo = (e) => {
    updateReq.value.photo = e.url
//   console.log('in parent compoennt, e=', e)
}

const exitGroupReq = ref({
    appId: switchStore.GroupIntroData.appId,
    groupId: switchStore.GroupIntroData.groupId,
    operator:userStore.userInfo.userId,
})

let getGroupReq = ref({
    memberId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    operator:userStore.userInfo.userId,
})
async function exitGroup(){
    console.log("exitGroupReq = ",exitGroupReq.value)
    ElMessageBox.confirm('此操作将退出群聊, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
            exitGroupMember(exitGroupReq.value).then((res) => {
            if (res.code == 200) {
                setTimeout(() => {
                    modal.msgSuccess("已退出群聊!");
                    switchStore.isGroupIntro = false
                    viewStore.getGroupList(getGroupReq.value)
                }, 500);
            }else{
                modal.msgError(res.msg);
            }
        })
    }).catch(() => {
        modal.msgWarning("操作已取消")
    });
}


const destroyGroupReq  = ref({
    appId:GroupIntroData.value.appId,
    groupId:GroupIntroData.value.groupId,
    operator:userStore.userInfo.userId,

})

function destroyGroupByOwner(){
    console.log("destroyGroupReq = ",destroyGroupReq.value)
    console.log("GroupIntroData = ",GroupIntroData.value)
    ElMessageBox.confirm('此操作将解散整个群聊，请谨慎操作, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
            destroyGroup(destroyGroupReq.value).then((res) => {
            if (res.code == 200) {
                setTimeout(() => {
                    modal.msgSuccess("群聊已解散!");
                    setup()
                }, 500);
            }else{
                modal.msgError(res.msg);
            }
        })
    }).catch(() => {
        modal.msgWarning("操作已取消")
    });
}

async function addMembers(){
    AddGroupMemberReq.value.isInvite = 1
    console.log("AddGroupMemberReq = ",AddGroupMemberReq.value)
    isAdd.value = false

    if(AddGroupMemberReq.value.members.length == 0){
        ElMessage.warning("未选择邀请成员！")
    }else{
        const { code, data, msg } = await addGroupMember(AddGroupMemberReq.value);
        if (code == 200) {
            switchStore.GroupIntroData.groupNickName = groupNickName.value
            ElMessage.success("群成员邀请成功！")
            setup()
        }
        else {
            modal.msgError(msg)
        }
        cancelAddMembers()
    }
    
}


function cancelNickName(){
    console.log("groupNickName = ",groupNickName)
    groupNickName.value = GroupIntroData.value.groupNickName == null && GroupIntroData.value.groupNickName == '' ? GroupIntroData.value.nickName : GroupIntroData.value.groupNickName
    isUpdateNickName.value = false
}



async function showMemberInfo(memberId,isFriend){
    var getRelationReq = {}
    getRelationReq.appId = userStore.userInfo.appId
    getRelationReq.fromId = userStore.userInfo.userId
    getRelationReq.toId = memberId
    getRelationWithInfo(getRelationReq).then((res)=>{
        if(res.code == 200){
            getMemberInfo(res.data,isFriend)
        }
    })
}

let isAddFriend = ref(false);
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
const addSource = ref([
    {value:1,label:'好友查找'},
    {value:2,label:'好友推荐'},
    {value:3,label:'组群信息'},
    {value:4,label:'组群查找'},
    {value:5,label:'好友邀请'},
    {value:6,label:'好友查找'},
])

function addFriend(toId) {
    // console.log(toId)
    isAddFriend.value = true;
    applyFriendData.value.toItem.toId = toId;
    applyFriendData.value.toItem.addSource = addSource.value[2].label
}

function cancelAdd(){
    isAddFriend.value = false
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
        ElMessage.success(data)
        viewStore.getFriendList(allFriendShipReq.value)
        cancelAdd()
    }
    else {
        modal.msgError("添加失败！")
        cancelAdd()
    }
}

let isMuteMember = ref(false);
let isDisMuteMember = ref(false);
let isRemoveMember = ref(false);
const muteReq = ref({
    groupId:GroupIntroData.value.groupId,
    appId:GroupIntroData.value.appId,
    operator:userStore.userInfo.userId
})

function muteMember(memberId){
    isMuteMember.value = true
    muteReq.value.memberId = memberId
    muteReq.value.mute = 1
}

function disMuteMember(memberId,speakDate){
    isDisMuteMember.value = true
    muteReq.value.memberId = memberId
    muteReq.value.speakDate = speakDate
    muteReq.value.mute = 0
}

function cancelDisMute(){
    isDisMuteMember.value = false
    muteReq.value.memberId = null
    muteReq.value.mute = null
}

// 禁言成员
async function confirmDisMute() {
    muteReq.value.speakDate = null
    console.log("muteReq.value = " ,muteReq.value)
    const { code, data, msg } = await muteGroupMember(muteReq.value);
    if (code == 200) {
        ElMessage.success("成员禁言解除")
        setup()
    }
    else {
        modal.msgError("解除禁言失败！")
        cancelDisMute()
    }
}

const removeGroupReq = ref({
    appId:GroupIntroData.value.appId,
    groupId:GroupIntroData.value.groupId,
    operator:userStore.userInfo.userId,
    clientType:1,
    memberId:null
})
function removeMember(memberId){
    isRemoveMember.value = true
    removeGroupReq.value.memberId = memberId
    // muteReq.value.speakDate = speakDate
    // muteReq.value.mute = 0
}


function cancelRemove(){
    isRemoveMember.value = false
    removeGroupReq.value.memberId = null
}

// 禁言成员
async function confirmRemove() {
    console.log("removeGroupReq.value = " ,removeGroupReq.value)
    const { code, data, msg } = await removeGroupMember(removeGroupReq.value);
    if (code == 200) {
        ElMessage.success("成员已被移除")
        setup()
    }
    else {
        modal.msgError("成员移除失败！")
        cancelRemove()
    }
}

function cancelMute(){
    isMuteMember.value = false
    muteReq.value.memberId = null
    muteReq.value.mute = null
}

// 禁言成员
async function confirmMute() {
    console.log("muteReq.value = " ,muteReq.value)
    const { code, data, msg } = await muteGroupMember(muteReq.value);
    if (code == 200) {
        ElMessage.success("该成员已被禁言")
        cancelMute()
        setup()
    }
    else {
        modal.msgError("禁言失败！")
        cancelMute()
    }
}



async function getMemberInfo(v ,isFriend){
    console.log("isFriend = ",isFriend)
    if(isFriend == 2){
        switchStore.isGroupChat = false
        switchStore.isChat = false
        switchStore.isFriendIntro = false
        switchStore.isFriends = false
        switchStore.isGroups = false
        switchStore.isVideo = false
        switchStore.isPersonal = true
    }else if(isFriend == 1){
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

async function UpdateNickName(){
    isUpdateNickName.value = false
    updateMemberReq.value.alias = groupNickName.value
    updateMemberReq.value.memberId = userStore.userInfo.userId
    console.log("updateMemberReq = ",updateMemberReq.value)
    const { code, data, msg } = await updateGroupMember(updateMemberReq.value);
    if (code == 200) {
        switchStore.GroupIntroData.groupNickName = groupNickName.value
        ElMessage.success("群昵称修改成功！")
        setup()
    }
    else {
        modal.msgError("群昵称修改失败！")
        cancelNickName()
    }
}


async function updateGroupInfo(){
    isUpdate.value = false
    console.log("updateReq = ",updateReq.value)
    const { code, data, msg } = await updateGroup(updateReq.value);
    if (code == 200) {
        switchStore.GroupIntroData = data
        console.log("GroupIntroData = " , switchStore.GroupIntroData)
        ElMessage.success("组群信息更新成功！")
        setup()
    }
    else {
        modal.msgError("更新失败！")
        cancelEdit()
    }
}

let getChatReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

function setup(){
    //使用刷新
    switchStore.isGroupIntro = false
    nextTick(()=>{
        //写入操作
        getGroupInfo()
        viewStore.getConversationList(getChatReq.value)
        viewStore.getGroupList(getGroupReq.value)
    })
}

let groupInfoReq = ref({
    appId: userStore.userInfo.appId,
    operator: userStore.userInfo.userId,
    groupId : switchStore.GroupIntroData.groupId
})

// 获取群聊信息
async function getGroupInfo(){
    await  groupInfo(groupInfoReq.value).then((res) => {
        if (res.code == 200) {
            switchStore.GroupIntroData = res.data
            switchStore.isGroupIntro = true
            console.log("GroupIntroData = " , switchStore.GroupIntroData)
        }
    })
}


const transferOwnerReq = ref({
    groupId : GroupIntroData.groupId,
    appId:GroupIntroData.appId,
    operator:userStore.userInfo.userId,
    ownerId:null
})

function transferOwner(memberId){
    transferOwnerReq.value.ownerId = memberId
    console.log("transferOwnerReq = ",transferOwnerReq.value)
    ElMessageBox.confirm('此操作将进行群主转让, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
        transferGroup(transferOwnerReq.value).then((res) => {
            if (res.code == 200) {
                setTimeout(() => {
                    modal.msgSuccess("群主已转移!");
                    setup()
                }, 500);
            }else{
                modal.msgError("群主转移失败!");
            }
        })
    }).catch(() => {
        modal.msgWarning("操作已取消")
    });
}



function setRole(memberId,role){
    updateMemberReq.value.memberId = memberId
    updateMemberReq.value.alias = null
    updateMemberReq.value.role = role
    updateMemberReq.value.clientType = 1
    console.log("updateMemberReq = ",updateMemberReq.value)
    ElMessageBox.confirm('此操作将对群管理员进行变动, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
            updateGroupMember(updateMemberReq.value).then((res) => {
                if (res.code == 200) {
                    setTimeout(() => {
                        if(role == 0){
                            modal.msgSuccess("成员已移出管理员!");
                        }else{
                            modal.msgSuccess("管理员设置成功!");
                        }
                        setup()
                    }, 500);
                }else{
                    modal.msgError(msg);
                }
            })
        }).catch(() => {
            modal.msgWarning("操作已取消")
        });
}


let getConversationReq = ref({
    // pageSize: 20,
    // pageNum: 1,
    fromId: userStore.userInfo.userId,
    toId : "",
    groupId : "",
    appId: userStore.userInfo.appId,
    conversationType: 1
})


function sentMessage(){
    switchStore.isFriendIntro = false
    switchStore.isGroupIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isGroupChat = false
    switchStore.isGroups = false
    switchStore.isChat = false

    getConversationReq.value.toId = switchStore.GroupIntroData.groupId
    getConversationReq.value.groupId = switchStore.GroupIntroData.groupId
    getConversationReq.value.conversationType = 1
    console.log("getConversationReq.value = ",getConversationReq.value)
    getConversation(getConversationReq.value).then((res) => {
        if (res.code == 200) {
            chatStore.chatGroup = res.data[0].dataInfo
            console.log(" chatStore.chatGroup = ", chatStore.chatGroup)
            switchStore.isGroupChat = true
        }
    })
}

const isAddGroup = ref(false)
function sendGroupApply(){
    AddGroupMemberReq.value.members[0] = userStore.userInfo.userId
    AddGroupMemberReq.value.isInvite = 0
    console.log("AddGroupMemberReq.value = ",AddGroupMemberReq.value)
    addGroupMember(AddGroupMemberReq.value).then((res) => {
        if (res.code == 200) {
            modal.msgSuccess("申请入群成功");
            isAddGroup.value = false
        }else{
            modal.msgError(res.msg);
        }
    })
}



</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

.imgbox {
    // display: flex;
    width: 60px;
    height: 60px;
    border-radius:60px ;
    float: left;
    // margin: 10px 15px 0 0;
    // align-items: center;
}


._message_content_box {
    margin: 0px 10px 0px 30px;
    font-size: 16px;
}

.add{
    width: 80px;
   height: 30px; 
   background-color: #9fc1d0;
//    float: right;
   border: none;
   margin-top: 20px;
   margin-left: 20px;
   border-radius: 5px;

}

// .add:hover{
//    background-color: #8ba4af;
// }
.content{
    float: left;
    margin-top: 20px;
}

.historyBox {
    margin-top: 2rem;
    width: 100%;
    height: 100%;
}
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
    /* border-radius: 50%!important; */
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
.nickNameInput{
    width: 250px;
    font-size: 20px;
    color: #e1e3eb;
    height: auto;
}

.mt20 {
    margin-top: 20px;
}
.nickName{
    width: 150px;
    font-size: 20px;
    color: #e1e3eb;
    padding: 0 10px;
    margin: 10px 0 0 0;
    text-align: center;
    height: auto;
    background-color: #8a98c9;
    border: solid #cacaca;
    border-radius: 15px;
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

.personal {
    margin-right: 30px;
}
</style>