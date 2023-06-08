<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="mgb20" style="height:500px;">
          <div class="user-info">
            <span style="font-size: 25px;margin-left:150px">账号信息</span>
          </div>
          <div class="user-info">
            <img v-if="adminInfo.photo == null" src="../assets/img/profile.jpg" />
            <img v-else :src="adminInfo.photo" class="user-avator" alt/>
            
            <div class="user-info-cont">
              <div class="user-info-name" v-if="adminInfo.param != null" style="color:#8a98c9;">{{adminInfo.param.role == null ? '未绑定' : adminInfo.param.role.name}}</div>
              <div class="user-info-name" v-else style="color:#8a98c9;">{{adminInfo.role == null ? '未绑定' : adminInfo.role.name}}</div>
              <div style="margin-top: 10px">
                <span style="color: #ccc">用户名: </span>
                <span style="color:#8a98c9;font-size: 16px;"> {{adminInfo.nickName}}</span>
              </div>
            </div>
          </div>
          <div class="user-info-list">
            我的账号
            <span style="color:#8a98c9;font-size: 20px;">{{adminInfo.userId}}</span>
          </div>
          <div class="user-info-list">
            手机号
            <span style="color:#8a98c9;font-size: 20px;">{{adminInfo.mobile == null ? '未绑定' : adminInfo.mobile}}</span>
          </div>
          <div class="user-info-list">
            邮箱
            <span style="color:#8a98c9;font-size: 20px;">{{adminInfo.email == null ? '未绑定' : adminInfo.email}}</span>
          </div>
          <div class="user-info-list">
            注册时间
            <span style="color:#8a98c9;font-size: 20px;">{{adminInfo.createTime}}</span>
          </div>
        </el-card>
        
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover" class="mgb20">
          <div class="user-info">
            <span style="font-size: 25px;">基本信息</span>
          </div>
          <el-tabs type="border-card">
            <el-tab-pane label="基本信息">
              <el-form :label-position="right" label-width="80px" :model="adminInfo">
                <el-form-item label="昵称">
                  <el-input v-model="adminInfo.nickName"></el-input>
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="adminInfo.mobile"></el-input>
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="adminInfo.email"></el-input>
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="adminInfo.userSex">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>
              <el-button type="primary" style="float:right;" @click="saveEdit()">保存</el-button>

            </el-tab-pane>
            <el-tab-pane label="修改密码">
              <el-form :model="ruleForm" status-icon  ref="ruleForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="密码" prop="pass">
                  <el-input type="password" v-model="ruleForm.password" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="checkPass">
                  <el-input type="password" v-model="ruleForm.checkPass" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitForm()">提交</el-button>
                  <el-button @click="resetForm('ruleForm')">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="新增账户">
              <el-form :label-position="right" label-width="80px" :model="user">
                  <el-form-item label="账号">
                    <el-input v-model="user.userId"></el-input>
                  </el-form-item>
                  <el-form-item label="昵称">
                    <el-input v-model="user.nickName"></el-input>
                  </el-form-item>
                  <el-form-item label="密码" prop="pass">
                    <el-input type="password" v-model="user.password" autocomplete="off"></el-input>
                  </el-form-item>
                  <el-form-item label="确认密码" prop="checkPass">
                    <el-input type="password" v-model="user.checkPass" autocomplete="off"></el-input>
                  </el-form-item>
                  <el-form-item label="权限">
                  <el-select v-model="user.roleId" placeholder="权限">
                    <el-option
                      v-for="item in roleList"
                      :key="item.roleId"
                      :label="item.name"
                      :value="item.roleId">
                  </el-option>
                  </el-select>
                </el-form-item>
                  <el-form-item label="状态">
                    <el-radio-group v-model="user.forbiddenFlag">
                    <el-radio :label="1">启用</el-radio>
                    <el-radio :label="0">禁用</el-radio>
                  </el-radio-group>
                  </el-form-item>
                  <el-form-item>
                  <el-button type="primary" @click="submitCreate()">提交</el-button>
                  <el-button @click="resetForm('user')">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
        
      </el-col>
    </el-row>

  </div>
</template>

<script>
import {changeUser,createUser,getInfo} from "../api/index";
import { ElMessage, ElMessageBox } from "element-plus";
import {getRoleByPage} from "../api/role";


export default {
  name: "dashboard",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      admin:{...this.adminInfo},
      ruleForm:{},
      user:{},
      roleList:[],
    }
  },
  methods: {
    getRoleInfo(){
      getRoleByPage({}).then(result=>{
        if(result.code === 200) {
          this.roleList = result.data
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      submitForm() {
        if(this.ruleForm.password == null || this.ruleForm.password == ''){
          ElMessage.error("密码为空")
          return
        }else if(this.ruleForm.checkPass == null || this.ruleForm.checkPass == ''){
          ElMessage.error("确认密码为空")
          return

        }else if(this.ruleForm.checkPass != this.ruleForm.password){
          ElMessage.error("密码不一致")
          return
        }
        
        // this.ruleForm.userId = this.adminInfo.userId
        // this.ruleForm.appId = this.adminInfo.appId
        // this.ruleForm.operator = this.adminInfo.userId
        // this.ruleForm.isAdmin = 1
        this.adminInfo.password = this.ruleForm.password
        changeUser(this.adminInfo).then((res)=>{
        if(res.code == 200){
          ElMessage.success("修改成功！")
          this.ruleForm = {}
        }else{
          ElMessage.error("修改失败！")
        }
      })
      },
      submitCreate() {
        if(this.user.userId == null || this.user.userId == ''){
          ElMessage.error("账号为空")
          return
        }else if(this.user.password == null || this.user.password == ''){
          ElMessage.error("密码为空")
          return
        }else if(this.user.checkPass == null || this.user.checkPass == ''){
          ElMessage.error("确认密码为空")
          return

        }else if(this.user.checkPass != this.user.password){
          ElMessage.error("密码不一致")
          return
        }
        this.user.appId = this.adminInfo.appId
        this.user.operator = this.adminInfo.userId
        createUser(this.user).then((res)=>{
        if(res.code == 200){
          ElMessage.success("创建成功！")
          this.user = {}
        }else{
          ElMessage.error("创建失败！")
        }
      })
      },
    saveEdit(){
      this.adminInfo.isAdmin = 1
      changeUser(this.adminInfo).then((res)=>{
        if(res.code == 200){
          ElMessage.success("修改成功！")
          localStorage.setItem('adminInfo',JSON.stringify(this.adminInfo))
        }else{
          ElMessage.error("修改失败！")
          this.adminInfo = {...this.admin}

        }
      })
    },
    getAdminInfo(){
      var userReq = {
          appId:this.adminInfo.appId,
          userId:this.adminInfo.userId
        };
        getInfo(userReq).then(res =>{
          if(res.code == 200){
            this.adminInfo = res.data
            localStorage.setItem('adminInfo',JSON.stringify(res.data))
          }
        })
    }
  },
  mounted() {
    this.getRoleInfo()
    this.getAdminInfo()
  }

};
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
}

.grid-content {
  display: flex;
  align-items: center;
  height: 100px;
}

.grid-cont-right {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.grid-num {
  font-size: 30px;
  font-weight: bold;
}

.grid-con-icon {
  font-size: 50px;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
  color: #fff;
}

.grid-con-1 .grid-con-icon {
  background: rgb(45, 140, 240);
}

.grid-con-1 .grid-num {
  color: rgb(45, 140, 240);
}

.grid-con-2 .grid-con-icon {
  background: rgb(100, 213, 114);
}

.grid-con-2 .grid-num {
  color: rgb(45, 140, 240);
}

.grid-con-3 .grid-con-icon {
  background: rgb(242, 94, 67);
}

.grid-con-3 .grid-num {
  color: rgb(242, 94, 67);
}

.user-info {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 2px solid #ccc;
  margin-bottom: 20px;
}

.user-avator {
  width: 120px;
  height: 120px;
  border-radius: 50%;
}

.user-info-cont {
  padding-left: 50px;
  flex: 1;
  font-size: 14px;
  color: #999;
}

.user-info-cont div:first-child {
  font-size: 30px;
  color: #222;
}

.user-info-list {
  font-size: 18px;
  line-height: 25px;
  margin-bottom: 30px;
  
}

.user-info-list span {
  margin-left: 70px;
}

.mgb20 {
  margin-bottom: 20px;
}

.todo-item {
  font-size: 14px;
}

.todo-item-del {
  text-decoration: line-through;
  color: #999;
}

.schart {
  width: 100%;
  height: 300px;
}
</style>
