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
          <el-form-item label="操作人">
            <el-select v-model="search.userId" placeholder="请选择操作人" style="width:300px">
                <el-option
                  v-for="item in userList"
                  :key="item.userId"
                  :label="item.nickName"
                  :value="item.userId">
                  <span style="float: left">{{ item.nickName }}</span>
                  <span
                    style="
                      float: right;
                      color: var(--el-text-color-secondary);
                      font-size: 13px;
                    "
                    >{{ item.userId }}</span
                  >
                </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="操作状态">
            <el-select v-model="search.status" placeholder="消息类型">
              <el-option label="成功" :value= '1'></el-option>
              <el-option label="失败" :value= '0'></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="发送时间">
            <el-date-picker
              v-model="search.time"
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
                <el-table-column prop="appId" fixed label="app编号" width="120" align="center"></el-table-column>
                <el-table-column prop="connectState" label="连接状态" width="120" align="center">
                  <template #default="scope">
                      <el-button v-if="scope.row.connectState == 1" type="success" plain size="small">在线</el-button>
                      <el-button v-else type="danger" plain size="small">离线</el-button>
                  </template>
                </el-table-column>
                <el-table-column prop="clientType" label="端类型" width="100" align="center">
                  <template #default="scope">
                      <el-tag v-if="scope.row.clientType == 0">webApi</el-tag>
                      <el-tag v-if="scope.row.clientType == 1">web</el-tag>
                      <el-tag v-if="scope.row.clientType == 2">ios</el-tag>
                      <el-tag v-if="scope.row.clientType == 3">android</el-tag>
                      <el-tag v-if="scope.row.clientType == 4">windows</el-tag>
                      <el-tag v-if="scope.row.clientType == 5">mac</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="imei" label="操作系统" width="100" align="center">
                  <template #default="scope">
                      <el-tag v-if="scope.row.imei != null">{{scope.row.imei}}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="operate" label="操作内容" width="400" align="center"></el-table-column>
                <el-table-column prop="status" label="操作状态" width="120" align="center">
                  <template #default="scope">
                      <el-button v-if="scope.row.status == 1" type="success" plain size="small">成功</el-button>
                      <el-button v-else type="danger" plain size="small">失败</el-button>
                  </template>
                </el-table-column>
                <el-table-column prop="userId" label="操作者" width="180" align="center"></el-table-column>
                <el-table-column prop="time" label="时间" width="180" align="center"></el-table-column>
            </el-table>
        </div>

       
    </div>

</template>

<script>
import { ElMessage, ElMessageBox } from "element-plus";
import {getLog,deleteLog}  from "../api/index"
import {getUserByPage} from "../api/index";


export default {
  name: "file",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      logReq:{},
      search:{},
      userList:[],
    }
  },
  methods:{
    getUserInfo(){
      getUserByPage({}).then((result)=>{
        if(result.code === 200) {
          this.userList = result.data
        }
      })
    },
    
    getLogInfo(){
      this.logReq.appId = this.adminInfo.appId
      getLog(this.logReq).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data
          // this.total = result.data.total
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    onSubmit(){
      this.search.appId = this.adminInfo.appId
      console.log(this.search)
      getLog(this.search).then((result)=>{
        if(result.code === 200) {
          this.tableData = result.data
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
    this.getLogInfo()
    this.getUserInfo()
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
