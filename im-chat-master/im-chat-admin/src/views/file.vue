<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-lx-cascades"></i> 消息文件
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
       

        <el-form :inline="true" :model="search" class="demo-form-inline">
          <el-form-item label="文件名称">
            <el-input v-model="search.fileName"></el-input>
          </el-form-item>
          <el-form-item label="消息类型">
            <el-select v-model="search.type" placeholder="消息类型">
              <el-option label="个人头像" :value= '1'></el-option>
              <el-option label="群头像" :value= '2'></el-option>
              <el-option label="消息图片" :value= '3'></el-option>
              <el-option label="消息文件" :value= '4'></el-option>
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
                <el-table-column prop="fileId" fixed label="文件编号" width="100" align="center"></el-table-column>
                <el-table-column prop="name" fixed label="文件名" width="250" align="center"></el-table-column>
                <el-table-column prop="location" label="文件预览" width="150" align="center">
                  <template #default="scope">
                        <a :href=fileUrl target="_Blank" @click="downloadFile(scope.row)" v-if="scope.row.type == 4" style="text-decoration:none;display: inherit;">
                          <img class="table-td-thumb"   src="../assets/img/file.png"/>
                        </a>
                        <el-image class="table-td-thumb" v-else :src="scope.row.location" :preview-src-list="[scope.row.location]">
                        </el-image>
                    </template>
                </el-table-column>

                <el-table-column prop="location" label="文件路径" width="400" align="center"></el-table-column>
                <el-table-column prop="extension" label="扩展名" width="100" align="center"></el-table-column>
                <el-table-column prop="type" label="类型" width="100" align="center">
                  <template #default="scope">
                      <el-tag> {{ scope.row.type == 1 ? '个人头像' : scope.row.type == 2 ? '群头像' :'消息文件' }} </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="type" label="文件大小" width="100" align="center">
                  <template #default="scope">
                      <el-tag v-if="scope.row.param.fileSize != null"> {{ scope.row.param.fileSize }} </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                      <!-- <el-button  type="primary" @click="handleInfo(scope.row)" round size="small">查看</el-button> -->
                      <!-- <el-button  type="warning" @click="handleEdit(scope.row)" round size="small">编辑</el-button> -->
                      <el-button  type="danger" @click="deleteFile(scope.row.fileId)" round size="small">删除</el-button>
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
import {getMessageFileByPage, deleteMsgFile}  from "../api/message"


export default {
  name: "file",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      nowId:'',
      messageFileReq:{},
      search:{},
      page:1,
      pageSize:30,
      total:0,
      roleList:[],
      isUserInfo:false,
      userInfo:{},
      isDownLoad:false,
      downLoadReq:{},
      fileUrl:''
    }
  },
  methods:{
    downloadFile(item){
      this.fileUrl = "http://192.168.211.1:8081/src/assets/data/" + item.name
    },
    
    getFileInfo(){
      this.messageFileReq.page = this.page
      this.messageFileReq.pageSize = this.pageSize
      getMessageFileByPage(this.messageFileReq).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data.records
          this.total = result.data.total
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    deleteFile(fileId){
      ElMessageBox.confirm(
        '你确定要删除本文件吗？',
        'Warning',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        deleteMsgFile(fileId,this.adminInfo.userId).then(result=>{
          if(result.code === 200) {
            ElMessage.success("删除成功")
            this.getFileInfo()
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
      this.search.page = this.page
      this.search.pageSize = this.pageSize
      console.log(this.search)
      getMessageFileByPage(this.search).then(result=>{
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
      this.getFileInfo()
    },
  },
  mounted() {
    this.getFileInfo()
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
