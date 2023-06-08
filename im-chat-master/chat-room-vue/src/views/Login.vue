<template>

  <body>
    <div class="container">
      <div>
        <div style="margin-bottom:80px">
          <BackGround v-if="(type == 3 || type == 2) && !isRegist" style="margin-left: 120px"></BackGround>
          <BackGround v-else></BackGround>
        </div>
        <!-- 输入框 -->
        <div style="display: flex;justify-content: center;">
          <div style="margin-left: 100px;margin-bottom: 20px;">
            <div v-if="isRegist" class="mb15">
              <div v-if="type == 2" class="mb15">
                <input class="login_input" v-model="mobile" type="text" placeholder="请输入电话号码" autofocus>
              </div>

              <div v-if="type == 3" class="mb15">
                <input class="login_input" v-model="email" type="text" placeholder="请输入电子邮箱" autofocus>
              </div>

              <div class="mb15">
                <input  class="login_input" type="password" v-model="code" placeholder="请输入验证码">
                <el-button v-if="canGet && type == 2"   @click="getLoginCode(mobile)" style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{btnText}}</el-button>
                <el-button v-else-if ="canGet && type == 3"   @click="getLoginCode(email)" style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{btnText}}</el-button>
                <el-button v-else  disabled style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{ btnText }}</el-button>
              </div>

      
              <div class="mb15">
                <input  class="login_input" type="password" v-model="password" @keyup.enter="handleRegist"
                placeholder="请输入密码">
              </div>

              <div class="mb15">
                <input  class="login_input" type="password" v-model="confirmPassword" @keyup.enter="handleRegist"
                placeholder="请确认密码">
              </div>

            </div>
           
            <div v-else style="margin-right: 100px;" class="mb15">
              <div v-if="type == 1">
                  <div class="mb15">
                    <input class="login_input" type="text" v-model="userId" placeholder="请输入你的账号" autofocus>
                  </div>
                  <input  class="login_input" type="password" v-model="password" @keyup.enter="handleRegist"
                    placeholder="请输入你的密码">
              </div>

              <div v-if="type == 2" style="margin-left: 100px;">
                  <div class="mb15">
                    <input class="login_input" type="text" v-model="mobile" placeholder="请输入手机号" autofocus>
                  </div>
                  <div class="mb15">
                    <input  class="login_input" type="password" v-model="code" placeholder="请输入验证码">
                    <el-button v-if="canGet && type == 2"   @click="getLoginCode(mobile)" style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{btnText}}</el-button>
                    <el-button v-else-if ="canGet && type == 3"   @click="getLoginCode(email)" style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{btnText}}</el-button>
                    <el-button v-else  disabled style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{ btnText }}</el-button>
                  </div>
              </div>

              <div v-if="type == 3"  style="margin-left: 100px;">
                  <div class="mb15">
                    <input class="login_input" type="text" v-model="email" placeholder="请输入邮箱" autofocus>
                  </div>
                  <div class="mb15">
                    <input  class="login_input" type="password" v-model="code" placeholder="请输入验证码">
                    <el-button v-if="canGet && type == 2"   @click="getLoginCode(mobile)" style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{btnText}}</el-button>
                    <el-button v-else-if ="canGet && type == 3"   @click="getLoginCode(email)" style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{btnText}}</el-button>
                    <el-button v-else  disabled style="background-color: #8a98c9; border-radius: 5px; margin-left: 20px;" type="primary" size="large" round>{{ btnText }}</el-button>
                  </div>
              </div>

            </div>
          </div>
        </div>


          <div v-if="!isRegist"  style="display: flex;justify-content: center;">
            <button @click="handleLogin" class="login_btn">{{lodding}}</button>
          </div>

          <div v-if="!isRegist && type == 1" style="display: flex;justify-content: center;margin-top: 30px;">
            <button  @click="ClickMobile" style="margin-right: 15px;" class="login_btn regist_btn">手机登录</button>
            <button  @click="ClickEmail" style="margin-left: 15px;" class="login_btn regist_btn">邮箱登录</button>
          </div>

          <div v-if="!isRegist && type == 2" style="display: flex;justify-content: center;margin-top: 30px;">
            <button  @click="ClickId" style="margin-right: 15px;" class="login_btn regist_btn">账号登录</button>
            <button  @click="ClickEmail" style="margin-left: 15px;" class="login_btn regist_btn">邮箱登录</button>
          </div>

          <div v-if="!isRegist && type == 3" style="display: flex;justify-content: center;margin-top: 30px;">
            <button  @click="ClickId" style="margin-right: 15px;" class="login_btn regist_btn">账号登录</button>
            <button  @click="ClickMobile" style="margin-left: 15px;" class="login_btn regist_btn">手机登录</button>
          </div>

          <div v-if="!isRegist" style="display: flex;justify-content: center;margin-top: 30px;">
            <button  @click="ClickRegist" class="login_btn regist_btn">注册</button>
          </div> 

          


        <div v-if="isRegist"  style="display: flex;justify-content: center;">
          <button  @click="handleLogin" class="login_btn regist_btn2">{{lodding}}</button>
        </div>
      
        <div v-if="isRegist" style="display: flex;justify-content: center;margin-top: 30px;">
          <button v-if="type == 3" @click="ClickMobile" class="login_btn regist_btn">手机注册</button>
          <button v-if="type == 2" @click="ClickEmail" class="login_btn regist_btn">邮箱注册</button>
        </div>
        
        <div v-if="isRegist" @click="ClickBack" style="display: flex;justify-content: center;margin-top: 10px;">
          <i class="fa fa-arrow-circle-left back"></i>
        </div>

      </div>
    </div>
  </body>

</template>

<script setup>
import BackGround from '../components/Login/loginBackground.vue'
import { ref, reactive} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Cookies from 'js-cookie'
import { login, getCode } from '../api/login'
import useUserStore from '../store/modules/user'
import useChatStore from "../store/modules/chat";
import moadl from '../plugins/modal'
import {imClient} from '../lim/core/ImClient'

const userStore = useUserStore()
const chatStore = useChatStore()
const router = useRouter()

const ListenerMap = {
	    onSocketConnectEvent: (option, status, data) => {
	        console.log("已建立连接:" + JSON.stringify(status));
	    },
	    onSocketErrorEvent: (error) => {
	        console.log("连接出现错误:" + error);
          moadl.alertError(error)
          router.push({
              path: "/login"
          })
	    },
	    onSocketReConnectEvent: () => {
	        console.log("正在重连:" );
	    },
	    onSocketCloseEvent: () => {
	        console.log("连接关闭:" );
	    },onSocketReConnectSuccessEvent: () => {
		    console.log("重连成功" );
		},
	    onTestMessage: (e) => {
	        console.log("onTestMessage ：" + e );
	    },
	    onP2PMessage: (e) => {
			console.log("onP2PMessage ：" + e );
			e = JSON.parse(e)
			uni.$emit('P2PMessage', e.data);
	    },
		onLogin: (uid) => {
		    console.log("用户"+uid+"登陆sdk成功" );
		}
		
	};
const isRegist = ref(false);
const type = ref(1)
const userId = ref()
const password = ref()
const code = ref()
const mobile = ref()
const email = ref()
const confirmPassword = ref()
var loginReq = {}
const getCodeReq = ref({})
var userReq = ref({
  appId:10000,
  userId:null
})

var countdown = 60; 
const lodding = ref("登录");
const registLodding = ref("Login");
const btnText = ref("获取验证码");
const canGet = ref(true);

async function ClickRegist() {
  isRegist.value = true;
  type.value = 2
  loginReq = {}
  console.log("type = ",type.value)
}

function ClickBack() {
  isRegist.value = false;
  loginReq = {}
  password.value = null
  type.value = 1
  console.log("type = ",type.value)
}

function ClickEmail() {
  loginReq = {}
  type.value = 3
  console.log("type = ",type.value)
}

function ClickId() {
  loginReq = {}
  type.value = 1
  console.log("type = ",type.value)
}

function ClickMobile() {
  loginReq = {}
  type.value = 2
  console.log("type = ",type.value)
}

async function getLoginCode(account){
  getCodeReq.value.type = type.value;
  if(type.value == 2){
    if (account == undefined ||account == null || account == '') {
        moadl.msgError("请输入手机号！")
        return
      }
    getCodeReq.value.mobile = account
  }
  if(type.value == 3){
    if (account == undefined ||account == null || account == '') {
        moadl.msgError("请输入电子邮件！")
        return
      }
    getCodeReq.value.email = account
  }
  getCodeReq.value.appId = 10000
  console.log(getCodeReq.value)
  await getCode(getCodeReq.value).then((res) => {
    if (res.code == 200) {
      canGet.value = false
      moadl.msgSuccess("验证码已发送！")
      settime()  
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


async function handleLogin() {
  // console.log(nickname.value, username.value, password.value);

  loginReq.type = type.value
  loginReq.mobile = mobile.value
  loginReq.email = email.value
  loginReq.password = password.value
  loginReq.userId = userId.value
  loginReq.code = code.value
  if(isRegist.value == true){
    // 注册
    loginReq.loginType = 2
    if(loginReq.type == 2){
      if (loginReq.mobile == undefined ||loginReq.mobile == null || loginReq.mobile == '') {
        moadl.msgError("请输入手机号！")
        return
      }
      loginReq.mobile = mobile.value
    }else if(loginReq.type == 3){
      if (loginReq.email == undefined ||loginReq.email == null || loginReq.email == '') {
        moadl.msgError("请输入电子邮件！")
        return
      }
    }
    if (loginReq.code == undefined ||loginReq.code == null || loginReq.code == '') {
        moadl.msgError("请输入验证码！")
        return
      }

      if (loginReq.password == undefined ||loginReq.password == null || loginReq.password == '') {
        moadl.msgError("请输入密码！")
        return
      }

      if (confirmPassword.value == undefined ||confirmPassword.value == null || confirmPassword.value == '') {
        moadl.msgError("请确认密码！")
        return
      }

      if(confirmPassword.value != loginReq.password){
        moadl.msgError("密码不一致！")
        return
      }
  }else{
    // 登录
    loginReq.loginType = 1

    if(loginReq.type == 1){
      if (loginReq.userId == undefined ||loginReq.userId == null || loginReq.userId == '') {
        moadl.msgError("请输入账号！")
        return
      }

      if (loginReq.password == undefined ||loginReq.password == null || loginReq.password == '') {
        moadl.msgError("请输入密码！")
        return
      }
    }else if(loginReq.type == 2){
      if (loginReq.mobile == undefined ||loginReq.mobile == null || loginReq.mobile == '') {
        moadl.msgError("请输入手机号！")
        return
      }

      if (loginReq.code == undefined || loginReq.code == null ||loginReq.code == '') {
        moadl.msgError("请输入验证码！")
        return
      }
    }else if(loginReq.type == 3){
      if (loginReq.email == undefined ||loginReq.email == null || loginReq.email == '') {
        moadl.msgError("请输入邮箱！")
        return
      }

      if (loginReq.code == undefined || loginReq.code == null ||loginReq.code == '') {
        moadl.msgError("请输入验证码！")
        return
      }
    }

  }

  loginReq.clientType = 1
  loginReq.appId = 10000
  await login(loginReq).then((res) => {
    if (res.code == 200) {
      
      lodding.value = "正在登录..."
      userStore.userInfo = res.data
      // console.log('userStore.userInfo ',userStore.userInfo);
      //app服务登录成功，去登录im    
      var listeners = {};
      for (const v in ListenerMap) {
          listeners[v] = ListenerMap[v];
      }

      imClient.init(res.data.routeInfo.ip,res.data.routeInfo.port,res.data.appId,res.data.userId,loginReq.clientType,res.data.userSign,listeners,function (imClient) {
          chatStore.chatSocket = imClient
          console.log('chatStore.chatSocket ',chatStore.chatSocket);
        if(imClient.state == 2){
            sessionStorage.setItem("imClient",JSON.stringify(imClient))
            console.log('sdk 成功连接的回调, 可以使用 sdk 请求数据了.');
            userReq.value.userId = res.data.userId
            userStore.getUserInfo(userReq.value)
            Cookies.set('userId', res.data.userId, { expires: 30 })
            moadl.msgSuccess("登录成功！")
            setTimeout(() => {
              router.push({
                path: "/load"
              })
            }, 1000);
        }else{
            moadl.msgError("浏览器不支持IM")
            loginReq = {}
            lodding.value = "登录"
            console.log('sdk 初始化失败.');
        }
      });
        
      }
  })
} 

</script>



<style>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

body {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 96.5vh;
  background-image: linear-gradient(to top, #fff1eb 0%, #ace0f9 100%);
}

.mb15 {
  margin-bottom: 15px;
}


.container {}

.back {
  font-size: 3em;
  color: #20fe37;
  -webkit-text-stroke: 2px black;
  cursor: pointer;
}

.login_input {
  width: 450px;
  height: 50px;
  padding-left: 50px;
  font-size: 36px;
  font-family: 'Bebas Neue', cursive;
  background: linear-gradient(45deg, transparent 5%, rgba(1, 19, 67, 0.8) 5%);
  border: 0;
  border-radius: 0px;
  /* line-height: 88px; */
  color: #fff;
  box-shadow: 6px 0px 0px #00a1ff;
  outline: transparent;
  position: relative;
}

.nick_name {
  animation: animateNickName 1s;
}

.nick_name::after {
  animation: 1s glitch;
}

@keyframes animateNickName {
  0% {
    transform: translateX(600px);
  }

  100% {
    transform: translateX(0px);
  }
}

.login_btn,
.login_btn::after {
  --slice-0: inset(50% 50% 50% 50%);
  --slice-1: inset(80% -6px 0 0);
  --slice-2: inset(50% -6px 30% 0);
  --slice-3: inset(50% -6px 85% 0);
  --slice-4: inset(40% -6px 43% 0);
  --slice-5: inset(80% -6px 5% 0);


  width: 350px;
  height: 70px;
  font-size: 36px;
  font-family: 'Bebas Neue', cursive;
  background: linear-gradient(45deg, transparent 5%, #FF013C 5%);
  border: 0;
  letter-spacing: 3px;
  /* line-height: 88px; */
  color: #fff;
  /* box-shadow: 6px 0px 0px #00E6F6; */
  outline: transparent;
  position: relative;
  cursor: pointer;
}

.regist_btn,
.regist_btn::after {
  width: 200px;
  height: 45px;
  font-size: 30px;
  /* box-shadow: #00E6F6 0px 0px 50px; */
}

.regist_btn2 {
  width: 400px;
  height: 70px;
  font-size: 30px;
  /* box-shadow: #00E6F6 0px 0px 100px; */
}

.regist_btn2::after {
  width: 400px;
  height: 70px;
}

.login_btn::after {
  content: 'Login Now';
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 3%, #00E6F6 3%, #00E6F6 5%, #FF013C 5%);
  text-shadow: -3px -3px 0px #F8F005, 3px 3px 0px #00E6F6;
  clip-path: var(--slice-0);
  transform: translate(-20px, 10px);
}

.login_btn:hover:after {
  animation: 1s glitch;
  animation-timing-function: steps(1, end);
}

@keyframes glitch {
  0% {
    clip-path: var(--slice-1);
    transform: translate(-20px, -10px);
  }

  10% {
    clip-path: var(--slice-3);
    transform: translate(10px, 10px);
  }

  20% {
    clip-path: var(--slice-5);
    transform: translate(-20px, -10px);
  }

  30% {
    clip-path: var(--slice-2);
    transform: translate(0px, 5px);
  }

  40% {
    clip-path: var(--slice-4);
    transform: translate(-5px, 0px);
  }

  50% {
    clip-path: var(--slice-3);
    transform: translate(5px, 0px);
  }

  60% {
    clip-path: var(--slice-4);
    transform: translate(5px, 10px);
  }

  70% {
    clip-path: var(--slice-4);
    transform: translate(-10px, 10px);
  }

  80% {
    clip-path: var(--slice-5);
    transform: translate(20px, -10px);
  }

  90% {
    clip-path: var(--slice-5);
    transform: translate(-10px, 0px);
  }

  100% {
    clip-path: var(--slice-1);
    transform: translate(0);
  }
}
</style>