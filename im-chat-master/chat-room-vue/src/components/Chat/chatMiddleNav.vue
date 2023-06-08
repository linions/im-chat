<template>
    <div class="middlebox">
        <div class="search flex">
            <input class="search_input" type="text" v-model="searchInfo.searchInfo" placeholder="请输入好友用户名 / 群聊名称 / 账号"
                @keyup.enter="handleSearch">
            <i class="fa fa-search search_btn" @click="handleSearch"></i>
        </div>

        <div class="shell">


            <ul v-if="!isFindFriend" class="shell-bottom">
                <li class="list" @click="handleMessage">
                    <img alt="" v-if="isMessage" src="../../assets/img/chat_active.png">
                    <img alt="" v-else src="../../assets/img/chat.png">
                </li>
                <li class="list" @click="handleFriends">
                    <img alt="" v-if="isFriends" src="../../assets/img/friendfavor_active.png">
                    <img alt="" v-else src="../../assets/img/friendfavor.png">
                </li>
                <li class="list" @click="handleGroup">
                    <img alt="" v-if="isGroup" src="../../assets/img/group_active.png">
                    <img alt="" v-else src="../../assets/img/group.png">
                </li>
            </ul>

            <div class="shell-top">
                <ul class="main">

                    <!-- 查找好友 -->
                    <li class="friend" v-if="isFindFriend">
                        <div @click="handleBack" class="backer"><i class="fa fa-grav"></i>&nbsp;返回</div>

                        <div class="friendBox" v-for="(item, index) in findFriendsList" :key="index" @click="showSearchIntro(item)">
                            <div class="message_img_box messageBoxLeft">
                                <img class="message_img" v-if="item.photo == null" src="../../assets/img/profile.jpg" alt="">
                                <img class="message_img" v-else :src=item.photo alt="">
                            </div>
                            <!-- 好友 -->
                            <div class="messageBoxRight" v-if="item.type == 1" >
                                <div class="message_info">
                                    <div v-if="item.friendShip != null && item.friendShip.remark != null">
                                            {{ item.friendShip.remark.length > 8
                                            ? item.friendShip.remark.substr(0, 9) + "..."
                                            : item.friendShip.remark}}
                                        </div>
                                        <div v-else>
                                            {{ item.nickName.length > 8
                                            ? item.nickName.substr(0, 9) + "..."
                                            : item.nickName}}
                                        </div>
                                    <div class="agreetxt">（{{item.userId}}）</div>
                                    
                                </div>

                                <div class="message_end">
                                    <div class="agreetxt" v-if="item.selfSignature != null">个性签名：{{ item.selfSignature.length >= 6
                                            ? item.selfSignature.substr(0, 6) + "..."
                                            : item.selfSignature}}</div>
                                    <div class="agreetxt" v-else>个性签名：对方暂未编辑</div>
                                    &nbsp;
                                    <div v-if="item.isFriend == 1">
                                        <div v-if="item.isFriend == 1 && item.status==1" class="online">在线</div>
                                        <div v-if="item.isFriend == 1 && item.status == 0" class="offline">离线</div>
                                    </div>
                                    <div v-else>
                                        <button  class="add">非好友</button>
                                    </div>
                                </div>
                            </div>

                            <!-- 群聊 -->
                            <div class="messageBoxRight" v-if="item.type == 2">
                                <div class="message_info">
                                    <div v-if="item.groupName != null">
                                            {{ item.groupName.length > 8
                                            ? item.groupName.substr(0, 9) + "..."
                                            : item.groupName}}
                                    </div>
                        
                                    <div class="agreetxt">（{{item.groupId}}）</div>
                                </div>

                                <div class="message_end">
                                    <div class="agreetxt" v-if="item.introduction != null">群介绍：{{ item.introduction.length > 8
                                            ? item.introduction.substr(0, 9) + "..."
                                            : item.introduction}}
                                    </div>
                                    <div class="agreetxt" v-else>群介绍：暂无群介绍</div>
                                    &nbsp;
                                    <div>({{item.groupMembers.length}})</div>
                                </div>
                            </div>
                            
                        </div>
                    </li>

                    
                    <li v-if="isMessage && !isFindFriend">
                        <div v-for="(item, index) in viewStore.conversationList" :key="index">
                            <el-popover
                                popper-style="background-color: rgb(186 194 222);font-size: 20px;text-align: center;width:30px;"
                                ref="popover"
                                placement="right"
                                trigger="hover">
                                <template #reference>

                                    <!-- 单聊消息 -->
                                    <div class="messageBox"  v-if="item.conversationType == 0" @click="handelChat(item,item.dataInfo,item.friendShip)">
                                        <img v-if="item.isTop" alt="" src="../../assets/img/star.png" class="top">
                                        <div class="message_img_box messageBoxLeft">
                                            <img class="message_img" v-if="item.dataInfo.photo == null" src="../../assets/img/profile.jpg" alt="">
                                            <img class="message_img" v-else :src=item.dataInfo.photo alt="">
                                        </div>
                                        <div class="messageBoxRight">
                                            <div class="message_info">
                                                <div v-if="item.friendShip != null && item.friendShip.remark != null">
                                                    {{ item.friendShip.remark.length > 10
                                                    ? item.friendShip.remark.substr(0, 11) + "..."
                                                    : item.friendShip.remark}}
                                                </div>
                                                <div v-else>
                                                    {{ item.dataInfo.nickName.length > 10
                                                    ? item.dataInfo.nickName.substr(0, 11) + "..."
                                                    : item.dataInfo.nickName}}
                                                </div>
                                                &nbsp;
                                                <div class="online" v-if="item.dataInfo.status == true">在线</div>
                                                <div class="offline" v-else>离线</div>
                                                <div class="point" v-if="item.isRead == 0"></div>

                                            </div>
                                            <div class="message_end" v-if="item.message != null">
                                                <div  style="font-size: 12px;color: #79a39f;">
                                                {{item.message.messageBody && ((tag,len)=>{
                                                    let brReg = /\<br\>/g,
                                                        spaceReg = /nbsp;/g
                                                        brMatch = tag.match(brReg),
                                                        spaceMatch = tag.match(spaceReg),
                                                        rtxSwitch = (tag)=>{
                                                            let rtx = tag,
                                                            match1 = [{m:rtx.match(/lt;/g),r:'<'},{m:rtx.match(/gt;/g),r:'>'}],
                                                            match2 = rtx.match(/nbsp;/g)
                                                            match3 = rtx.match(/\<br\>/g)
                                                            if(item.message.type == 2){
                                                                return '[图片]'
                                                            }
                                                            if(item.message.type == 3){
                                                                return '[文件]'
                                                            }
                                                            if(item.message.type == 4){
                                                                return '[语音通话]'
                                                            }
                                                            if(item.message.type == 5){
                                                                return '[视频通话]'
                                                            }
                                                            match1.map(item=>{
                                                                if(item.m){
                                                                    rtx = rtx.replaceAll('&'+item.m[0],item.r)
                                                                }
                                                            })
                                                            match2 && (()=>{
                                                                rtx = rtx.replaceAll('&'+match2[0],'')
                                                            })()
                                                            match3 && (()=>{
                                                                rtx = rtx.replaceAll('&'+match3[0],'')                                             
                                                            })()
                                                            return rtx
                                                        }
                                                        res = rtxSwitch(String(tag))
                                                        if(len > 8){
                                                            res = res.substr(0,9) + '...'
                                                        }else{
                                                            res = res
                                                        }
                                                    return res
                                                })(item.message.messageBody,item.message.messageBody.length)                                       
                                                }}
                                                </div>
                                                <div style="color: #79a39f;font-size: 10px;">{{item.message.messageTime}}</div>
                                            </div>
                                            <div v-else class="message_end" >
                                                <div style="font-size: 12px;color: #79a39f;">
                                                    暂无消息记录
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                         <!-- 群聊消息 -->
                                    <div class="messageBox" v-if="item.conversationType == 1" @click="handelGroupChat(item,item.dataInfo)">
                                        <img v-if="item.isTop" alt="" src="../../assets/img/star.png" class="top">
                                        <div class="message_img_box messageBoxLeft">
                                            <img class="message_img" v-if="item.dataInfo == null || item.dataInfo.photo == null" src="../../assets/img/profile.jpg" alt="">
                                            <img class="message_img" v-else :src=item.dataInfo.photo alt="">
                                        </div>
                                        <div class="messageBoxRight">
                                            <div class="message_info">
                                                <div v-if="item.dataInfo != null && item.dataInfo.groupName != null">
                                                    {{ item.dataInfo.groupName.length >= 10
                                                    ? item.dataInfo.groupName.substr(0, 11) + "..."
                                                    : item.dataInfo.groupName}}
                                                    <div class="point" v-if="item.isRead == 0"></div>
                                                </div>
                                                
                                            </div>
                                            <div class="message_end" v-if="item.message != null">
                                                <div style="font-size: 12px;color: #79a39f;">
                                                    {{ item.message.userData.userId }}: 
                                                    <!-- {{ item.message.messageBody }} -->
                                                    {{item.message.messageBody && ((tag,len)=>{
                                                    let brReg = /\<br\>/g,
                                                        spaceReg = /nbsp;/g
                                                        brMatch = tag.match(brReg),
                                                        spaceMatch = tag.match(spaceReg),
                                                        rtxSwitch = (tag)=>{
                                                            let rtx = tag,
                                                            match1 = [{m:rtx.match(/lt;/g),r:'<'},{m:rtx.match(/gt;/g),r:'>'}],
                                                            match2 = rtx.match(/nbsp;/g)
                                                            match3 = rtx.match(/\<br\>/g)
                                                            if(item.message.type == 2){
                                                                return '[图片]'
                                                            }
                                                            if(item.message.type == 3){
                                                                return '[文件]'
                                                            }
                                                            if(item.message.type == 4){
                                                                return '[语音通话]'
                                                            }
                                                            if(item.message.type == 5){
                                                                return '[视频通话]'
                                                            }
                                                            match1.map(item=>{
                                                                if(item.m){
                                                                    rtx = rtx.replaceAll('&'+item.m[0],item.r)
                                                                }
                                                            })
                                                            match2 && (()=>{
                                                                rtx = rtx.replaceAll('&'+match2[0],'')
                                                            })()
                                                            match3 && (()=>{
                                                                rtx = rtx.replaceAll('&'+match3[0],'')                                             
                                                            })()
                                                            return rtx
                                                        }
                                                        res = rtxSwitch(String(tag))
                                                        if(len > 8){
                                                            res = res.substr(0,9) + '...'
                                                        }else{
                                                            res = res
                                                        }
                                                    return res
                                                })(item.message.messageBody,item.message.messageBody.length)                                       
                                                }}
                                                </div>
                                                <div style="color: #79a39f;font-size: 10px;">{{item.message.messageTime}}</div>
                                            </div>
                                            <div v-else>
                                                <div style="font-size: 12px;color: #79a39f;">
                                                    暂无聊天记录
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                </template>
                                <el-row v-if="item.isTop != 0" @click="topChatList(item.conversationId,0)" style="border-bottom: dashed;" class="setting">
                                    <el-col  :span="24" >
                                        取消置顶
                                    </el-col>
                                </el-row>
                                <el-row  v-else @click="topChatList(item.conversationId,1)" style="border-bottom: dashed;" class="setting">
                                    <el-col :span="24" >
                                        置顶
                                    </el-col>
                                </el-row>
                                <el-row @click="deleteChatList(item.conversationId,item.toId)" style="border-bottom: dashed;" class="setting">
                                    <el-col :span="24" >
                                        删除会话
                                    </el-col>
                                </el-row >
                            </el-popover>
                                       
                        </div>

                        <div class="nullSty" v-if="viewStore.conversationList.length == 0">
                            <img alt="" style="margin-top: 30px;width:289px;height:289px" src="../../assets/img/1.png">
                            <div class="box-one"></div>
                            <div class="box-two"></div>
                            <div class="box-one"></div>
                            <div>快去找好友聊天哟~</div>
                        </div>
                    </li>

                    <!-- 好友 -->
                    <li v-if="isFriends && !isFindFriend">
                        <div class="messageBox" v-for="(item, index) in viewStore.friendList" :key="index" @click="showFriendIntro(item)">
                            <div class="message_img_box messageBoxLeft">
                                <img class="message_img" v-if="item.param.userInfo.photo == null" src="../../assets/img/profile.jpg" alt="">
                                <img class="message_img" v-else :src="item.param.userInfo.photo" alt="">
                            </div>
                            <div class="messageBoxRight">
                                <div class="message_info">
                                    <div v-if="item.remark != null">{{ item.remark }}</div>
                                    <div v-else>{{ item.param.userInfo.nickName }}</div>
                                    &nbsp;
                                    <div class="agreetxt">{{item.param.userInfo.userId}}</div>
                                    
                                </div>
                                <div class="message_info">
                                    <div class="agreetxt" v-if="item.param.userInfo.selfSignature != null">个性签名：  
                                        {{item.param.userInfo.selfSignature.length >= 6  ? item.param.userInfo.selfSignature.substr(0,7)+ "...": item.param.userInfo.selfSignature}}
                                    </div>
                                    <div class="agreetxt" v-else>个性签名：对方暂未编辑</div>

                                    &nbsp;
                                    <div v-if="item.param.userInfo.status" class="online">在线</div>
                                    <div v-else class="offline">离线</div>
                                    
                                </div>
                                

                            </div>
                        </div>
                        <div class="nullSty" v-if="viewStore.friendList.length == 0">
                            <img alt="" style="margin-top: 30px;width:289px;height:289px" src="../../assets/img/2.png">
                            <div class="box-one"></div>
                            <div class="box-two"></div>
                            <div class="box-one"></div>
                            <div>暂无好友哟~</div>
                        </div>

                    </li>

                    <!-- 群聊 -->
                    <li v-if="isGroup && !isFindFriend">
                        <div class="flex">
                            <el-button class="addGroup_btn" type="primary" @click="isAddGroup = true">+ 创建群聊</el-button>
                        </div>
                        <div class="messageBox" v-for="(item, index) in viewStore.groupList" :key="index">
                            <div class="message_img_box messageBoxLeft" @click="showGroupIntro(item)">
                                <img class="message_img" v-if="item.photo == null" src="../../assets/img/profile.jpg" alt="">
                                <img class="message_img" v-else :src=item.photo alt="">
                            </div>
                            <div class="groupBoxRight" @click="handelGroupChat(item,item)">
                                <div class="message_info">
                                    <div v-if="item.groupName!=null">{{ item.groupName }}</div>
                                    <div v-else>{{ item.ownerId }}的群聊</div>
                                    &nbsp;
                                    <div>({{ item.param.memberCount }})</div>
                                </div>
                                <div>
                                    <span style="color: #79a39f;font-size: 13px;">{{ item.groupId }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="nullSty" v-if="viewStore.groupList.length == 0">
                            <img alt="" src="../../assets/img/3.png">
                            <div class="box-one"></div>
                            <div class="box-two"></div>
                            <div class="box-one"></div>
                            <div>暂无群聊唷~</div>
                        </div>
                    </li>

                    <!-- 添加群聊 -->
                    <el-dialog v-model="isAddGroup" title="添加群聊" width="25%">
                        <el-row style=" margin-top: 10px;">
                            <el-col>
                                <span style="float: left; left;margin-top: 5px;">群名称：</span>
                                <el-input style="float: left; width: 300px;" v-model="createGroupData.groupName" placeholder="请输入群名称"></el-input>
                            </el-col>
                        </el-row>
                        <el-row >
                            <el-col >
                                <span style="float: left; margin-top: 5px;">群聊类型：</span>
                                <el-select v-model="createGroupData.groupType" placeholder="群聊类型">
                                    <el-option
                                    style="padding-left: 20px;"
                                    v-for="item in groupTypeList"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-col>
                        </el-row>

                        <el-row  style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left; margin-top: 5px;">禁言类型：</span>
                                <el-select v-model="createGroupData.mute" placeholder="禁言类型">
                                    <el-option
                                    style="padding-left: 20px;"
                                    v-for="item in muteTypeList"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-col>
                        </el-row>

                        <el-row  style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left; margin-top: 5px;">申请加群：</span>
                                <el-select v-model="createGroupData.applyJoinType" placeholder="申请加群">
                                    <el-option
                                    style="padding-left: 20px;"
                                    v-for="item in applyJoinTypeList"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-col>
                        </el-row>

                        <el-row  style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left; margin-top: 5px;">群聊人数：</span>
                                <el-select v-model="createGroupData.MaxMemberCount"  placeholder="群聊人数">
                                    <el-option
                                    style="padding-left: 20px;"
                                    v-for="item in maxMemberList"
                                    :key="item.value"
                                    :label="item.value"
                                    :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-col>
                        </el-row>

                        <el-row  style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left; margin-top: 5px;">群聊管理员：</span>
                                <el-select
                                    v-model="createGroupData.admins"
                                    multiple
                                    collapse-tags
                                    style="float: left;"
                                    placeholder="请选择管理员"
                                    @change="selectAdmins">
                                    <el-option
                                    style="padding-left: 20px;"
                                    v-for="item in viewStore.friendList"
                                    :key="item.param.userInfo.userId"
                                    :label="item.param.userInfo.nickName"
                                    :value="item.param.userInfo.userId"
                                    :disabled="item.disabled">
                                        <span style="float: left">{{ item.param.userInfo.nickName }}</span>
                                        <span style="float: left; padding-left: 10px; color: #8492a6; font-size: 13px">({{ item.param.userInfo.userId }})</span>  
                                </el-option>
                                </el-select>
                            </el-col>
                        </el-row>

                        <el-row  style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left; margin-top: 5px;">邀请好友：</span>
                                <el-select 
                                    v-model="createGroupData.members"
                                    multiple
                                    collapse-tags
                                    style="float: left;"
                                    placeholder="请选择群成员"
                                    @change="selectMembers">
                                    <el-option
                                    style="padding-left: 20px;"
                                    v-for="item in MembersDataList"
                                    :key="item.param.userInfo.userId"
                                    :label="item.param.userInfo.nickName"
                                    :value="item.param.userInfo.userId"
                                    :disabled="item.disabled">
                                    <span style="float: left">{{ item.param.userInfo.nickName }}</span>
                                    <span style="float: left; padding-left: 10px; color: #8492a6; font-size: 13px">({{ item.param.userInfo.userId }})</span>  
                                    </el-option>
                                </el-select>
                            </el-col>
                        </el-row>
                        
                        <el-row style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left; margin-top: 15px;" >群介绍：</span>
                                <el-input type="textarea"
                                style="float: left;width: 300px;"
                                :autosize="{ minRows: 2, maxRows: 4}"
                                placeholder="请输入群介绍"
                                v-model="createGroupData.introduction">
                                </el-input>
                            </el-col>
                        </el-row>
                        
                        <el-row style=" margin-top: 10px;">
                            <el-col >
                                <span style="float: left;margin-top: 15px;">群公告：</span>
                                <el-input type="textarea"
                                style="float: left;width: 300px;"
                                :autosize="{ minRows: 2, maxRows: 4}"
                                placeholder="请输入群公告"
                                v-model="createGroupData.notification">
                                </el-input>
                            </el-col>
                        </el-row>
                       
                        <template #footer>
                            <span class="dialog-footer">
                                <el-button @click="cancelCreateGroup">取消</el-button>
                                <el-button type="primary" @click="addGroup">确定</el-button>
                            </span>
                        </template>
                    </el-dialog>

                </ul>
            </div>
        </div>

    </div>
</template>

<script setup>

import { ref, reactive, watch, nextTick } from 'vue';
import { findFriends, friendsList } from '../../api/friends/friend'
import { createGroup, groupList,searchGroup,groupInfo,uninstallLogo } from '../../api/group/group'
import moadl from '../../plugins/modal'
import useUserStore from '../../store/modules/user'
import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'
import img from '../../assets/img/profile.jpg'
import useSwitchStore from "../../store/modules/switch";
import useChatStore from "../../store/modules/chat";
import useViewStore from '../../store/modules/viewInfo'
import { getConversation,deleteConversation,topConversation} from '../../api/conversation/conversation'

let isFindFriend = ref(false);
let isAddGroup = ref(false);
const userStore = useUserStore()
const switchStore = useSwitchStore()
const chatStore = useChatStore()
const viewStore = useViewStore()

let searchInfo = ref({
    userId: userStore.userInfo.userId,
    appId:userStore.userInfo.appId,
    searchInfo: "",
    operator:userStore.userInfo.userId,
});

let findFriendsList = ref([]);

let isMessage = ref(true);
let isFriends = ref(false);
let isGroup = ref(false);

// 列表数据----------------------------------------------
const MembersDataList = ref([])

let groupInfoReq = ref({
    appId: userStore.userInfo.appId,
    groupId : null,
    operator:userStore.userInfo.userId
})

const createGroupData = reactive({
    operator: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    photo: null,
    ownerId: userStore.userInfo.userId,
    groupType: null,
    groupName: null,
    mute: null,
    applyJoinType: null,
    MaxMemberCount:null,
    introduction: null,
    notification: null,
    admins:[],
    members:[]
})
let photoName = null

const member = ref({
    memberId:null,
    role:null,
    joinType:null
    
})

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

function selectAdmins(adminList){
    createGroupData.admins = adminList
    for(var i = 0 ; i < MembersDataList.value.length ; i++){
        if(adminList.indexOf(MembersDataList.value[i].toId) != -1){
            MembersDataList.value[i].disabled = true
        }
        if(adminList.indexOf(MembersDataList.value[i].toId) != -1 && createGroupData.members.indexOf(MembersDataList.value[i].toId) != -1){
            let index = createGroupData.members.indexOf(MembersDataList.value[i].toId)
            createGroupData.members.splice(index,1)
        }
    }

}

function selectMembers(memberList){
    createGroupData.members = memberList
}

const joinGroupData = reactive({
    UserGuId: userStore.userInfo,
    GroupName: null,
    GroupImg: null,
})


let getChatReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

let getConversationReq = ref({
    fromId: userStore.userInfo.userId,
    toId : "",
    groupId : "",
    appId: userStore.userInfo.appId,
    conversationType: 1
})

const readData = reactive({
    userInfo: userStore.userInfo,
    friendInfo: null,
})

function handleMessage() {
    isMessage.value = true;
    isFriends.value = false;
    isGroup.value = false;
}

function handleFriends() {
    isFriends.value = true;
    isMessage.value = false;
    isGroup.value = false;
}

function handleGroup() {
    isGroup.value = true;
    isFriends.value = false;
    isMessage.value = false;
}



// 搜索
async function handleSearch() {
    if (searchInfo.value.searchInfo == "") {
        ElMessage.error('请输入搜索条件')
        return
    }
    
    let friends = ref([]);
    

    const { code, data } = await findFriends(searchInfo.value);
    if (code == 200) {
        ElMessage.success('查找成功')
        for(let i =0 ; i < data.length; i++){
            friends.value.push(data[i])
            findFriendsList.value = friends.value
        } 
    }
    else {
        ElMessage.error('该用户不存在')
    }

    await searchGroup(searchInfo.value).then((res) => {
        if (res.code == 200) {
            for(let i =0 ; i < res.data.length;i++){
                friends.value.push(res.data[i])
                findFriendsList.value = friends.value
            }
        }
    })
    isFindFriend.value = true;
}

// 返回
function handleBack() {
    isFindFriend.value = false;
    searchInfo.value.searchInfo = null
}

function cancelCreateGroup(){
    isAddGroup.value = false
    if(photoName != null){
        uninstallLogo(photoName)
    }
    createGroupData.photo = null
    createGroupData.groupType = null
    createGroupData.groupName = null
    createGroupData.mute = null
    createGroupData.applyJoinType = null
    createGroupData.MaxMemberCount = null
    createGroupData.introduction = null
    createGroupData.notification = null
    createGroupData.admins = []
    createGroupData.members = []
    photoName = null
}


// 添加群聊
async function addGroup() {
    console.log("createGroupData = ",createGroupData)
    const { code, data, msg } = await createGroup(createGroupData);
    if (code == 200) {
        ElMessage.success(msg)
        cancelCreateGroup()
    }
    else {
        moadl.msgError(msg)
    }
}



const allFriendShipReq = ref({
    fromId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId
})

let getGroupReq = ref({
    memberId: userStore.userInfo.userId,
    appId: userStore.userInfo.appId,
    operator:userStore.userInfo.userId,
})

// 获取群聊信息
async function getGroupInfo(){
    await  groupInfo(groupInfoReq.value).then((res) => {
        if (res.code == 200) {
            switchStore.GroupIntroData = res.data
            console.log("GroupIntroData = " , switchStore.GroupIntroData)
            showGroupIntro()
        }
    })
}


// 简介
async function showFriendIntro(v) {
    // console.log(v);
    switchStore.isFriendIntro = true
    switchStore.isGroupIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isChat = false
    switchStore.isVideo = false
    switchStore.isGroupChat = false
    switchStore.isNotification = false
    switchStore.FriendIntroData = v
}

// 简介
async function showGroupIntro() {    
    // console.log(v);
    switchStore.isGroupIntro = true
    switchStore.isFriendIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isNotification = false
    switchStore.isChat = false
    switchStore.isGroupChat = false
}

async function showSearchIntro(v) {
    console.log("showSearchIntro = ",v);
    if(v.type == 1 && v.isFriend == 2){
        switchStore.isGroupChat = false
        switchStore.isChat = false
        switchStore.isFriendIntro = false
        switchStore.isFriends = false
        switchStore.isGroups = false
        switchStore.isVideo = false
        switchStore.isNotification = false
        switchStore.isPersonal = true
    }else if(v.type == 1 && v.isFriend == 1){
        var friendData = {...v.friendShip}
        friendData.param = {}
        friendData.param.userInfo = {...v}
        console.log("friendData = ",friendData)
        switchStore.FriendIntroData = friendData
        showFriendIntro(friendData)
    }else if(v.type == 1 && v.isFriend == 0){
        var friendData = {}
        friendData.black = 0
        friendData.status = 0
        friendData.param = {}
        friendData.param.userInfo = {...v}
        console.log("friendData = ",friendData)
        switchStore.FriendIntroData = friendData
        showFriendIntro(friendData)
    }else{
        groupInfoReq.value.groupId = v.groupId
        getGroupInfo()
    }
}


watch(async (v) => {
    await viewStore.getFriendList(allFriendShipReq.value);

    await viewStore.getGroupList(getGroupReq.value);

    await viewStore.getConversationList(getChatReq.value);
    MembersDataList.value = viewStore.friendList

    console.log(viewStore.conversationList)

},
{
    immediate: true,
    deep: true
})



watch(() => switchStore.FriendIntroData, async (value) => {
    //使用刷新
    switchStore.isFriendIntro = false
    nextTick(()=>{
        //写入操作
        switchStore.isFriendIntro = true
    })
})

//  开始聊天------------
async function handelChat(item,friendInfo,friendShip) {
    item.isRead = 1
    switchStore.isFriendIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isGroupChat = false
    switchStore.isGroups = false
    chatStore.chatUser = friendInfo
    chatStore.friendShip = friendShip
    switchStore.isNotification = false
    switchStore.isChat = true
}



async function handelGroupChat(item,groupInfo) {
    item.isRead = 1
    switchStore.isChat = false
    switchStore.isFriendIntro = false
    switchStore.isPersonal = false
    switchStore.isFriends = false
    switchStore.isGroups = false
    switchStore.isGroupIntro = false
    switchStore.isVideo = false
    switchStore.isNotification = false
    getConversationReq.value.toId = groupInfo.groupId
    getConversationReq.value.groupId = groupInfo.groupId
    getConversation(getConversationReq.value).then((res) => {
        if (res.code == 200) {
            chatStore.chatGroup = res.data[0].dataInfo
            console.log(" chatStore.chatGroup = ", chatStore.chatGroup)
            switchStore.isGroupChat = true
            viewStore.getConversationList(getChatReq.value)
        }
    })
}


const deleteConversationReq = ref({
    appId:userStore.userInfo.appId,
    fromId:userStore.userInfo.userId,
    toId:null,
    conversationId:null,
})
const topConversationReq = ref({
    appId:userStore.userInfo.appId,
    fromId:userStore.userInfo.userId,
    conversationId:null,
    isTop:null,
})
async function deleteChatList(conversationId,toId) {
    deleteConversationReq.value.toId = toId
    deleteConversationReq.value.conversationId = conversationId
    await deleteConversation(deleteConversationReq.value).then((res) => {
        if (res.code == 200) {
            ElMessage.success("会话已删除")
            viewStore.getConversationList(getChatReq.value)
        }else{
                ElMessage.error("会话删除失败")
        }
    })
}

async function topChatList(conversationId,isTop) {
    topConversationReq.value.isTop = isTop
    topConversationReq.value.conversationId = conversationId
    await topConversation(topConversationReq.value).then((res) => {
        if (res.code == 200) {
            if(isTop == 0 ) {
                ElMessage.success("已取消置顶")
            }else{
                ElMessage.success("已置顶")
            }
            viewStore.getConversationList(getChatReq.value)
        }else{
            if(isTop == 0 ) {
                ElMessage.success("取消置顶失败")
            }else{
                ElMessage.success("置顶失败")
            }
        }
    })
}


</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

.setting{
    height: 40px;
    width: 100%;
    text-align: 40px;
    line-height: 40px;
}

.setting :hover{
    color: white;
}

.el-upload--picture-card {
    --el-upload-picture-card-size: 100px;
}
.el-input{
    margin-bottom: 15px ;
}

.agreetxt{
    font-size: 13px!important;
    color: #79a39f;
    float: left;
}
.online{
    font-size: 13px!important;
    border: 1px solid;
    border-radius: 5px;
    width: 35px;
    height: 25px;
    line-height: 25px;
    text-align: center;
    background-color: rgb(15 194 54);
    color: #efe5e5;
}

.offline{
    font-size: 13px!important;
    border: 1px solid;
    border-radius: 5px;
    width: 35px;
    height: 25px;
    line-height: 25px;
    text-align: center;
    background-color: gray;
    color: #efe5e5;
}
.flex {
    display: flex;
    justify-content: center;
    align-items: center;
}

.search_btn {
    cursor: pointer;
    color: #bebcb9;
    font-size: 1.3rem;
    position: absolute;
    right: 30px;
}

.backer {
    font-size: 1.2rem;
    display: flex;
    align-items: center;
    font-family: 'Bebas Neue', cursive;
    cursor: pointer;
    margin: 15px 0;
}

.middlebox {
    width: 330px;
    height: 680px;
    padding: 10px;
    overflow: hidden;
}

.search {
    margin-top: 5px;
    position: relative;
}

.search_input {
    width: 90%;
    height: 40px;
    padding-left: 10px;
    font-size: 15px;
    border: 0;
    border-radius: 5px;
    outline: transparent;
    box-shadow: 1px 2px 5px rgba(0, 0, 0, 0.4);
}

* {
    padding: 0;
    margin: 0;
    list-style: none;
    transition: .5s;
}

.messageBox {
    width: 90%;
    height: 80px;
    display: flex;
    align-items: center;
    padding-left: 5%;
    border-radius: 10px;
    cursor: pointer;
    margin: 5px 0;
    border-bottom: solid 2px #8a98c9;
}

.messageBox:hover {
    transition: .3s;
    background-color: rgba(157, 172, 225, 0.5);
}

.friendBox {
    width: 90%;
    height: 80px;
    display: flex;
    align-items: center;
    padding-left: 5%;
    border-radius: 10px;
}

.friendBox:hover {
    transition: .3s;
    background-color: rgba(157, 172, 225, 0.5);
}

.message_img_box {
    position: relative;
}

.message_img {
    width: 50px !important;
    height: 50px !important;
    background-color: #fff;
    border-radius: 50%;
    overflow: hidden;
    object-fit: cover;
}

.messageBoxRight {
    display: flex;
    flex-direction: column;
    width: 80%;
    height: 60%;
    margin-left: 10px;
}

.groupBoxRight {
    display: flex;
    flex-direction: column;
    width: 80%;
    margin-left: 10px;
}

.message_info {
    display: flex;
    width: 90%;
    justify-content: space-between;
}
.top{
    position: absolute;
    margin-bottom: 60px;
    width: 20px !important;
}
.point {
    position: absolute;
    top: 5px;
    right: 15px;
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

.message_end {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 5px;
    width: 95%;
}

.shell {
    width: 94%;
    height: 90%;
    background-color: #fff;
    box-shadow: 1px 2px 10px rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    margin-top: 20px;
    margin-left: 10px;
    border-radius: 10px !important;
    overflow-y: scroll;
}

.shell::-webkit-scrollbar {
    width: 0;
    height: 0;
    color: transparent;
}

.shell-top {
    width: 100%;
    height: 100%;
    border-bottom: 1px solid rgba(223, 223, 223);
    overflow-x: hidden;
}

.shell-top ul {
    width: 100%;
    height: 100%;
    display: flex;
    position: relative;
    transition: .3s;
    left: 0;
}

.shell-top ul li {
    width: 100%;
    margin: 0 0 0 10px;
    overflow-y: scroll;
    position: relative;
}

.shell-top ul li::-webkit-scrollbar {
    width: 0;
    height: 0;
    color: transparent;
}


.friend {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start !important;
    animation: ani 1s;
}

.shell-top ul li img {
    width: 150px;
}

.shell-top ul li .box-one {
    width: 70%;
    height: 10px;
    border-radius: 10px;
    background-color: rgb(220, 220, 220);
    margin: 15px 0;
}

.shell-top ul li .box-two {
    width: 50%;
    height: 10px;
    border-radius: 10px;
    background-color: rgb(220, 220, 220);
}

.shell-bottom {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-evenly;
    height: 58px;
}

.shell-bottom li {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    filter: grayscale(0.5)
}

.shell-bottom li img {
    width: 30px;
    cursor: pointer;
}

.add {
   width: 40px;
   background-color: #d5eaf3;
   border: none;
   border-radius: 2px;
}

.dialog-footer button {
    width: 50px;
}

.nullSty {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 15px;
}

.addGroup_btn {
    width: 100px;
    letter-spacing: 1px;
    font-size: 13.5px;
}

.main li {
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
</style>