<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-lx-cascades"></i> 消息记录
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
       

        <el-form :inline="true" :model="search" class="demo-form-inline">
          <el-form-item label="消息键">
            <el-input v-model="search.messageKey"></el-input>
          </el-form-item>
          <el-form-item label="消息类型">
            <el-select v-model="search.type" placeholder="消息类型">
              <el-option label="文本" :value= '1'></el-option>
              <el-option label="图片" :value= '2'></el-option>
              <el-option label="文件" :value= '3'></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="发送时间">
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
                <el-table-column prop="messageKey" fixed label="消息键" width="180" align="center"></el-table-column>
                <el-table-column prop="messageBody" label="消息/文件路径" width="600" align="center">
                  <template #default="scope">
                    <el-image class="table-td-thumb" v-if="scope.row.type == 2" :src="scope.row.messageBody" :preview-src-list="[scope.row.messageBody]"/>
                    <span v-else>{{ scope.row.messageBody }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="100" align="center">
                  <template #default="scope">
                      <el-tag> {{ scope.row.type == 1 ? '文本' : scope.row.type == 2 ? '图片' :'文件' }} </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="文件大小" width="100" align="center">
                  <template #default="scope">
                      <el-tag v-if="scope.row.param.fileSize != null"> {{ scope.row.param.fileSize }} </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="messageTime" label="发送时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                      <!-- <el-button  type="primary" @click="handleInfo(scope.row)" round size="small">查看</el-button> -->
                      <!-- <el-button  type="warning" @click="handleEdit(scope.row)" round size="small">编辑</el-button> -->
                      <el-button  type="danger" @click="handleDelete(scope.row.messageKey)" round size="small">删除</el-button>
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


        
    </div>

    <div class="block" style="text-align: center;">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[30, 50, 70, 100]"
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
import {getMessageByPage, deleteMessageByKey}  from "../api/message"


export default {
  name: "file",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      nowId:'',
      messageReq:{},
      search:{},
      page:1,
      pageSize:30,
      total:0,
      roleList:[],
      isUserInfo:false,
      userInfo:{},
    }
  },
  methods:{
    
    getMessageInfo(){
      this.messageReq.appId = this.adminInfo.appId
      this.messageReq.page = this.page
      this.messageReq.pageSize = this.pageSize
      getMessageByPage(this.messageReq).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data.records
          this.total = result.data.total
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    handleDelete(messageKey){
      ElMessageBox.confirm(
        '你确定要删除本条信息吗？',
        'Warning',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        deleteMessageByKey(this.adminInfo.appId,messageKey,this.adminInfo.userId).then(result=>{
          if(result.code === 200) {
            ElMessage.success("删除成功")
            this.getMessageInfo()
          }else {
              ElMessage.error(`${result.msg}`)
            }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
        
      })
      
    },

    onSubmit(){
      this.search.appId = this.adminInfo.appId
      this.search.page = this.page
      this.search.pageSize = this.pageSize
      console.log(this.search)
      getMessageByPage(this.search).then(result=>{
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
      this.getMessageInfo()
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
    this.getMessageInfo()
    // this.getRoleInfo()
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
