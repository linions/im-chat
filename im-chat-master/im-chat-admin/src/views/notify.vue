<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-lx-cascades"></i> 通知公告
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <el-form :inline="true" :model="search" class="demo-form-inline">
          <el-form-item label="发送方">
            <el-input v-model="search.fromId" placeholder="发送方"></el-input>
          </el-form-item>
          <el-form-item label="接收方">
            <el-input v-model="search.nickName" placeholder="接收方"></el-input>
          </el-form-item>
          <el-form-item label="处理人">
            <el-input v-model="search.operator" placeholder="处理人"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="search.status" placeholder="状态">
              <el-option label="已处理" :value= '1'></el-option>
              <el-option label="未处理" :value= '0'></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="search.type" placeholder="类型">
              <el-option label="系统公告" :value= '1'></el-option>
              <el-option label="用户反馈" :value= '2'></el-option>
              <el-option label="系统通知" :value= '3'></el-option>
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
            <el-button type="success" style="margin-bottom: 5px;" plain @click="addNotify">新建通知</el-button>
            <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="id" fixed label="编号" width="100" align="center"></el-table-column>
                <el-table-column prop="title" label="主题" width="250" align="center"></el-table-column>
                <el-table-column prop="content" label="内容" width="400" align="center"></el-table-column>
                <el-table-column prop="fromId" label="发送方" width="180" align="center"></el-table-column>
                <el-table-column prop="toId" label="接收方" width="180" align="center"></el-table-column>
                <el-table-column prop="clientType" label="消息类型" width="120" align="center">
                  <template #default="scope">
                      <el-tag v-if="scope.row.type == 1">系统公告</el-tag>
                      <el-tag v-if="scope.row.type == 2">用户反馈</el-tag>
                      <el-tag v-if="scope.row.type == 3">系统通知</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120" align="center">
                  <template #default="scope">
                      <el-button v-if="scope.row.status == 1" type="success" plain size="small">已处理</el-button>
                      <el-button v-else type="danger" plain size="small">未处理</el-button>
                  </template>
                </el-table-column>
                <el-table-column prop="feedBack" label="处理反馈" width="300" align="center"></el-table-column>
                <el-table-column prop="operatorId" label="处理人" width="180" align="center"></el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="350" align="center">
                    <template #default="scope">
                      <el-button :disabled="scope.row.type == 2"  type="primary" @click="handleEdit(scope.row)" round size="small">编辑</el-button>
                      <el-button :disabled="scope.row.status == 1"  type="success" @click="handleStatus(scope.row)" round size="small">处理</el-button>
                      <el-button   type="warning" @click="handleNotice(scope.row)" round size="small">通知</el-button>
                      <el-button   type="danger" @click="handleDelete(scope.row.id)" round size="small">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        

        <!-- 用户反馈 -->
        <el-dialog v-model="isUserReport" title="处理反馈" width="20%">
            <div style="margin-bottom: 10px;">
                反馈内容:
                <el-input type="textarea"
                :autosize="{ minRows: 4, maxRows: 8}"
                placeholder="请输入反馈内容"
                v-model="handleReq.feedBack">
                </el-input>
            </div>
           
        
            <template #footer>
                <span class="dialog-footer">
                    <el-button  @click="isUserReport = false">取消</el-button>
                    <el-button  type="primary" @click="confirmHandle()"> 确认 </el-button>
                </span>
            </template>
        </el-dialog>


        <el-dialog title="编辑" v-model="editVisible" width="30%">
            <el-form label-width="70px">
                <el-form-item label="主题：">
                    <el-input v-model="editData.title"></el-input>
                </el-form-item>
                <el-form-item  label="内容：">
                  <el-input type="textarea"
                  :autosize="{ minRows: 4, maxRows: 8}"
                  placeholder="请输入反馈内容"
                  v-model="editData.content">
                </el-input>
                </el-form-item>
                <el-form-item  label="消息类型：" label-width="100px">
                  <el-select v-model="editData.type" placeholder="状态">
                    <el-option label="系统公告" :value= '1'></el-option>
                    <!-- <el-option label="用户反馈" :value= '2'></el-option> -->
                    <el-option label="系统通知" :value= '3'></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="editData.type == 3" label="接收方：" label-width="100px">
                  <!-- <el-input v-model="editData.content"></el-input> -->
                  <el-select v-model="editData.toId" placeholder="请选择接收方" style="width:300px">
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
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editVisible = false">取 消</el-button>
                    <el-button type="primary" @click="saveNotify()">保存</el-button>
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
import { ElMessage, ElMessageBox,ElNotification} from "element-plus";
import {getUserByPage} from "../api/index";
import {createNotify,sendNotify,updateNotify,handleNotify,getNotify,deleteNotify} from "../api/notify";


export default {
  name: "user",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      handleReq:{},
      notifyReq:{},
      search:{},
      page:1,
      pageSize:15,
      total:0,
      userList:[],
      userInfo:{},
      isUserReport:false,
      isUpdate:false,
    }
  },
  methods:{
    addNotify(){
      this.editData = {}
      this.editVisible = true
      this.isUpdate = false
    },
    getUserInfo(){
      getUserByPage({}).then((result)=>{
        if(result.code === 200) {
          this.userList = result.data
        }
      })
    },
    getNotifyInfo(){
      this.notifyReq.page = this.page
      this.notifyReq.pageSize = this.pageSize
      this.notifyReq.appId = this.adminInfo.appId
      getNotify(this.notifyReq).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data.records
          this.total = result.data.total
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    handleStatus(row){
      this.isUserReport = true
      this.handleReq = row
    },
    confirmHandle(){
      this.handleReq.appId = this.adminInfo.appId
      this.handleReq.operator = this.adminInfo.userId
      handleNotify(this.handleReq).then((result)=>{
        if(result.code == 200){
          ElMessage.success("消息处理成功")
          this.isUserReport = false
          this.getNotifyInfo()
        } else {
          ElMessage.error(`${result.msg}`)
        }
      }).catch((err) =>{
        ElMessage.error(err)
      })
    },
    handleEdit(row) {
      this.editData = row
      this.editVisible = true
      this.isUpdate = true
    },
    handleNotice(row){
      ElMessageBox.confirm(
        '你确定要发布本条通知吗？',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'Warning',
        }
      ).then(() => {
        sendNotify(this.adminInfo.appId,row.id,this.adminInfo.userId).then((res)=>{
          if(res.code == 200){
            ElMessage.success('通知已发布')
            this.getNotifyInfo()
            if(row.type == 1){
                    ElNotification({
                        title: "系统公告",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>主题：</span> "+ row.title +"<br><span style= 'color: #8a98c9'>内容：</span>" + row.content,
                        type: 'info',
                    })
                }
                if(row.type == 2){
                    ElNotification({
                        title: "用户反馈",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>主题：</span> "+ row.title +"<br><span style= 'color: #8a98c9'>内容：</span>" + row.content + "<br><span style= 'color: #8a98c9'>反馈：</span>" + row.feedBack,
                        type: 'info',
                    })
                }
                if(row.type == 3){
                    ElNotification({
                        title: "系统通知",
                        dangerouslyUseHTMLString: true,
                        message: "<span style= 'color: #8a98c9'>主题：</span> "+ row.title +"<br><span style= 'color: #8a98c9'>内容：</span>" + row.content
                    })
                }
          } else {
          ElMessage.error(`${res.msg}`)
        }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
       
      })

    },
    handleDelete(id){
      ElMessageBox.confirm(
        '你确定要删除本条通知吗？',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'Warning',
        }
      ).then(() => {
        deleteNotify(id,this.adminInfo.appId,this.adminInfo.userId).then((res)=>{
          if(res.code == 200){
            ElMessage.success('通知已删除')
            this.getNotifyInfo()
          } else {
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
    saveNotify(){
      this.editData.isAdmin = 1
      this.editData.operator = this.adminInfo.userId
      this.editData.appId = this.adminInfo.appId
      console.log(this.editData)
     if(this.isUpdate){
      updateNotify(this.editData).then(result =>{
        if(result.code === 200) {
          ElMessage({
            type:'success',
            message:'修改成功'
          })
          this.editVisible = false
          this.getNotifyInfo()
        } else {
          ElMessage({
            type:'error',
            message:`${result.msg}`
          })
        }
      })
     }else{
      createNotify(this.editData).then(result =>{
        if(result.code === 200) {
          ElMessage({
            type:'success',
            message:'新增成功'
          })
          this.editVisible = false
          this.getNotifyInfo()
        } else {
          ElMessage({
            type:'error',
            message:`${result.msg}`
          })
        }
      })
     }
    },
    onSubmit(){
      this.search.appId = this.adminInfo.appId
      this.search.page = this.page
      this.search.pageSize = this.pageSize
      console.log(this.search)

      getNotify(this.search).then(result=>{
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
      this.getNotifyInfo()
    },
  },
  mounted() {
    this.getNotifyInfo()
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
