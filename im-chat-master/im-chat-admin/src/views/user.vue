<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-lx-cascades"></i> 用户管理
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <el-form :inline="true" :model="search" class="demo-form-inline">
          <el-form-item label="账号">
            <el-input v-model="search.userId" placeholder="账号"></el-input>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="search.nickName" placeholder="昵称"></el-input>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="search.mobile" placeholder="手机号"></el-input>
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="search.email" placeholder="邮箱"></el-input>
          </el-form-item>
          <el-form-item label="在线状态">
            <el-select v-model="search.status" placeholder="在线状态">
              <el-option label="在线" :value= "1"></el-option>
              <el-option label="离线" :value= "0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="search.forbiddenFlag" placeholder="状态">
              <el-option label="启用" :value= "0"></el-option>
              <el-option label="禁用" :value= "1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="权限">
            <el-select v-model="search.roleId" placeholder="权限">
              <el-option
                v-for="item in roleList"
                :key="item.roleId"
                :label="item.name"
                :value="item.roleId">
            </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="search.createTime"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onSubmit">查询</el-button>
          </el-form-item>
      </el-form>

        <div class="container">
            <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="userId" fixed label="账号" width="150" align="center"></el-table-column>
                <el-table-column prop="nickName" label="昵称" width="150" align="center"></el-table-column>
                <el-table-column prop="email" label="用户邮箱" width="180" align="center">
                  <template #default="scope">
                        {{ scope.row.email == null ? '未绑定': scope.row.email }}
                    </template>
                </el-table-column>
                <el-table-column label="头像(查看大图)" width="150" align="center">
                    <template #default="scope">
                        <img class="table-td-thumb" v-if="scope.row.photo == null" src="../assets/img/profile.jpg"/> 
                        <el-image class="table-td-thumb" v-else :src="scope.row.photo" :preview-src-list="[scope.row.photo]">
                        </el-image>
                    </template>
                </el-table-column>
                <el-table-column prop="mobile" label="电话" width="150" align="center">
                  <template #default="scope">
                        {{ scope.row.mobile == null ? '未绑定': scope.row.mobile }}
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="在线状态" width="100" align="center">
                  <template #default="scope">
                      <el-button v-if="scope.row.status == 1" type="success" plain size="small">在线</el-button>
                      <el-button v-else type="danger" plain size="small">离线</el-button>
                  </template>
                </el-table-column>
                <el-table-column prop="forbiddenFlag" width="100" label="状态" align="center">
                  <template #default="scope">
                    <el-switch  
                      v-model="scope.row.forbiddenFlag " :active-value="0" :inactive-value="1" @click="beforeChange(scope.row.userId,scope.row.forbiddenFlag,scope.row)"/>
                    </template>
                </el-table-column>
                <el-table-column label="权限" width="150" align="center">
                    <template #default="scope">
                      <el-tag> {{ scope.row.param.role != null ? scope.row.param.role.name : '非角色用户' }} </el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="createTime" label="注册时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="250" align="center">
                    <template #default="scope">
                      <el-button :disabled="scope.row.name == '超级管理员' || (scope.row.name == '普通管理员' && adminInfo.isAdmin == 1)"  type="primary" @click="handleInfo(scope.row)" round size="small">查看</el-button>
                      <el-button :disabled="scope.row.name == '超级管理员' || (scope.row.name == '普通管理员' && adminInfo.isAdmin == 1)"  type="warning" @click="handleEdit(scope.row)" round size="small">编辑</el-button>
                      <el-button :disabled="scope.row.name == '超级管理员' || (scope.row.name == '普通管理员' && adminInfo.isAdmin == 1)"  type="danger" @click="handleDelete(scope.row.userId)" round size="small">注销</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 用户详情弹出框 -->
        <el-dialog title="用户详情" v-model="isUserInfo" width="40%" center>
            <el-form label-width="70px">
                <el-row>
                  <el-form-item  label="账号：" style="width: 300px;" label-width="100px">
                    <span class="info">{{userInfo.userId}}</span>
                  </el-form-item>
                  <el-form-item  label="用户昵称：" label-width="100px" >
                    <span class="info">{{userInfo.nickName}}</span>
                  </el-form-item>
                </el-row>
                <el-form-item label="头像地址：" label-width="100px">
                  <span class="info">{{userInfo.photo}}</span>
                </el-form-item>
               <el-row>
                  <el-form-item style="width: 320px;" label-width="100px" label="电话：">
                    <span class="info">{{userInfo.mobile}}</span>
                  </el-form-item>
                  <el-form-item   label="邮箱：" >
                    <span class="info">{{userInfo.email}}</span>
                  </el-form-item>
               </el-row>
               <el-row>
                  <el-form-item label="生日：" style="width: 300px;" label-width="100px">
                    <span class="info">{{userInfo.birthDay}}</span>
                  </el-form-item>
                  <el-form-item label="性别：" label-width="100px">
                    <span class="info">{{ userInfo.userSex == 0 ? '未设置' : (userInfo.userSex == 1 ? '男' : '女')}}</span>
                  </el-form-item>
              </el-row>
              <el-row>
                  <el-form-item label="是否禁用：" label-width="100px" style="width: 300px;">
                    <span class="info">{{userInfo.forbiddenFlag == 0 ? '否' : '是'}}</span>
                  </el-form-item>
                  <el-form-item label="在线状态：" label-width="100px">
                    <span class="info">{{ userInfo.status == 0 ? '离线' :  '在线'}}</span>
                  </el-form-item>
              </el-row>
              <el-row>
                  <el-form-item  label="角色" style="width: 300px;">
                    <span class="info">{{userInfo.param.role.name != null ? userInfo.param.role.name : '未绑定'}}</span>
                  </el-form-item>
                  <el-form-item label="创建时间：" label-width="100px">
                    <span class="info">{{ userInfo.createTime}}</span>
                  </el-form-item>
              </el-row>
              <el-form-item label="地址：" label-width="100px">
                  <span class="info">{{userInfo.location}}</span>
                </el-form-item>
                
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="isUserInfo = false">关 闭</el-button>
                </span>
            </template>
        </el-dialog>


        <el-dialog title="编辑" v-model="editVisible" width="30%">
            <el-form label-width="70px">
                <el-form-item label="用户名：">
                    <el-input v-model="editData.nickName"></el-input>
                </el-form-item>
                <!-- <el-form-item label="头像：">
                  <el-image class="photo" v-if="editData.photo != null" :src="editData.photo" :preview-src-list="[editData.photo]">
                  </el-image>
                  <img class="photo" v-else src="../assets/img/profile.jpg" alt="">
                  <el-upload
                    ref="upload"
                    style="display: contents;"
                    :action = "`/v1/user/uploadLogo/` + editData.userId +'/' + editData.appId"
                    :limit="1"
                    :on-exceed="handleExceed"
                    :on-success="handleSuccess" 
                    :show-file-list="false"
                    :auto-upload="true"
                    name="photo">
                      <el-button color="#97a8be" type="primary" size="small">更换头像</el-button>
                  </el-upload>
                 
                  <el-dialog v-model="dialogVisible">
                    <img width="100%" :src="dialogImageUrl" alt="">
                  </el-dialog>
                </el-form-item> -->
                <el-form-item  label="电话：">
                  <el-input v-model="editData.mobile"></el-input>
                </el-form-item>
                <el-form-item  label="邮箱：">
                  <el-input v-model="editData.email"></el-input>
                </el-form-item>
                <el-form-item  label="权限：">
                  <el-select v-model="editData.roleId" placeholder="请选择"  style="width: 40%;">
                    <el-option
                      v-for="item in roleList"
                      :key="item.roleId"
                      :label="item.name"
                      :value="item.roleId">
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item  label="状态：">
                <el-radio-group v-model="editData.forbiddenFlag">
                  <el-radio :label="0">启用</el-radio>
                  <el-radio :label="1">禁用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editVisible = false">取 消</el-button>
                    <el-button type="primary" @click="saveUser()">保存</el-button>
                </span>
            </template>
        </el-dialog>
    </div>

    <div class="block" style="text-align: center;">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[15, 20, 30, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
</template>

<script>
import { ElMessage, ElMessageBox } from "element-plus";
import {changeUser, getUserByPage,destroyUser , changeStatus} from "../api/index";
import {getRoleByPage} from "../api/role";


export default {
  name: "user",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      nowId:'',
      userReq:{},
      search:{},
      page:1,
      pageSize:15,
      total:0,
      roleList:[],
      isUserInfo:false,
      userInfo:{},
    }
  },
  methods:{
    //  图片上传按钮
    handleSuccess(res){
      if(res.code == 200){
        ElMessage.success('头像更换成功')
        this.getUserInfo()
        this.editVisible = false
      }else{
        ElMessage.success('头像更换失败')
      }
    },
    
    handleExceed(files) {
      files = []
    },
    getUserInfo(){
      this.userReq.appId = this.adminInfo.appId
      this.userReq.page = this.page
      this.userReq.pageSize = this.pageSize
      getUserByPage(this.userReq).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data.records
          this.total = result.data.total
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    handleInfo(row){
      this.isUserInfo = true
      this.userInfo = row
    },
    handleEdit(row) {
      this.editData = row
      // console.log(this.editData)
      this.editVisible = true
      // console.log(id)
    },
    handleDelete(userId){
      ElMessageBox.confirm(
        '你确定要注销该用户吗？该操作将不可逆，请谨慎操作。',
        'Warning',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        destroyUser(userId,this.adminInfo.userInfo).then((res)=>{
          if(res.code == 200){
            ElMessage.success('该用户已注销')
            this.getUserInfo()
          }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
       
      })

    },
    beforeChange(userId,status,row){
      console.log(status)
      var text = '启用'
      if(status == 1){
        text = '禁用'
      }
      
       ElMessageBox.confirm(
        '你确定要' + text + '该角色吗？',
        'Warning',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        changeStatus(userId,status,this.adminInfo.userId).then((res)=>{
          if(res.code == 200){
            ElMessage.success(text + '成功')
            this.getUserInfo()
          }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
        if(status == 1){
          row.forbiddenFlag = 0
        }else{
          row.forbiddenFlag = 1
        }
      })
    },
    saveUser(){
      this.editData.isAdmin = 1
      this.editData.operator = this.adminInfo.userId
      console.log(this.editData)
      changeUser(this.editData).then(result =>{
        // console.log(result)
        if(result.code === 200) {
          ElMessage({
            type:'success',
            message:'修改成功'
          })
          this.editVisible = false
          this.getUserInfo()
        } else {
          ElMessage({
            type:'error',
            message:`${result.msg}`
          })
        }
      })
    },
    onSubmit(){
      this.search.appId = this.adminInfo.appId
      this.search.page = this.page
      this.search.pageSize = this.pageSize
      console.log(this.search)

      getUserByPage(this.search).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data.records
          this.total = result.data.total
          this.search = {}
          ElMessage.success("查询成功")

        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    handleSizeChange(val) {
        this.pageSize = val
        this.handleCurrentChange(1)
      },
    handleCurrentChange(val) {
      this.page = val
      this.getUserInfo()
    },
    getRoleInfo(){
      getRoleByPage({}).then(result=>{
        if(result.code === 200) {
          this.roleList = result.data
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
  },
  mounted() {
    this.getUserInfo()
    this.getRoleInfo()
  }

};
</script>

<style scoped>
.info{
  font-size: 18px; 
  color: #8a98c9;

}
.handle-box {
    margin-bottom: 20px;
}

.handle-select {
    width: 120px;
}

.handle-input {
    width: 300px;
    display: inline-block;
}
.table {
    width: 100%;
    font-size: 14px;
}
.red {
    color: #ff0000;
}
.mr10 {
    margin-right: 10px;
}
.table-td-thumb {
    display: block;
    margin: auto;
    width: 40px;
    height: 40px;
}

.photo {
    width: 100px;
    height: 100px;
}

</style>
