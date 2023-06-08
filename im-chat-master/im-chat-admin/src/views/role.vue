<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-lx-cascades"></i> 角色管理
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <el-form :inline="true" :model="search" class="demo-form-inline">
   
          <el-form-item label="角色名称">
            <el-input v-model="search.name" placeholder="角色名称"></el-input>
          </el-form-item>
   
          <el-form-item label="状态">
            <el-select v-model="search.status" placeholder="状态">
              <el-option label="启用" :value = '1'></el-option>
              <el-option label="禁用" :value = '0'></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="onSubmit">查询</el-button>
          </el-form-item>
      </el-form>

        <div class="container">
            <el-button type="primary" style="margin-bottom: 5px;" plane @click="addRole">创建角色</el-button>
            <el-table  :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="roleId" label="id"  align="center"></el-table-column>
                <el-table-column prop="name" label="角色名"  align="center"></el-table-column>
                <el-table-column prop="sort" label="显示排序" align="center"></el-table-column>
               
                <el-table-column prop="status" label="状态" align="center">
                  <template #default="scope">
                      <el-switch :disabled="scope.row.name == '超级管理员' || (scope.row.name == '普通管理员' && adminInfo.isAdmin == 1)" 
                      v-model="scope.row.status " :active-value="1" :inactive-value="0" @click="beforeChange(scope.row.roleId,scope.row.status,scope.row)"/>
                  </template>
                   
                </el-table-column> 
                <el-table-column prop="param.userCount" label="用户个数" align="center">
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="250" align="center">
                    <template #default="scope">
                      <el-button :disabled="scope.row.name == '超级管理员' || (scope.row.name == '普通管理员' && adminInfo.isAdmin == 1)"  type="primary" @click="handleEdit(scope.row)" round size="small">编辑</el-button>
                      <el-button type="success" @click="bindUser(scope.row)" round size="small">绑定用户</el-button>
                      <el-button :disabled="scope.row.name == '超级管理员' || (scope.row.name == '普通管理员' && adminInfo.isAdmin == 1)"  type="danger" @click="handleDelete(scope.row.roleId)" round size="small">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 编辑弹出框 -->
        <el-dialog title="编辑角色" v-model="editVisible" width="30%">
            <el-form label-width="70px">
                <el-form-item label="角色名" >
                    <el-input v-model="editData.name" ></el-input>
                </el-form-item>
                <el-form-item label="角色顺序">
                  <el-input-number v-model="editData.sort" :min="1"  @change="handleChange" />
                </el-form-item>
                <el-form-item  label="数据范围">
                  <el-select v-model="editData.extent" placeholder="状态">
                    <el-option label="全部" :value = '1'></el-option>
                    <el-option label="仅本人" :value = '2'></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item  label="状态">
                  <el-radio-group v-model="editData.status">
                    <el-radio :label="1">启用</el-radio>
                    <el-radio :label="0">禁用</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item  label="备注">
                  <el-input v-model="editData.remark"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editVisible = false">取 消</el-button>
                    <el-button type="primary" @click="saveRole()">保存</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 分配角色 -->
        <el-dialog title="分配角色" v-model="assignRole" width="30%">
            <el-form label-width="70px">
                <el-form-item label="角色名" >
                    <el-input v-model="assignData.roleName" style="width: 200px;" :disabled="true"></el-input>
                </el-form-item>
                <el-form-item label="用户">
                  <el-select v-model="assignData.users"
                      multiple
                      collapse-tags
                      placeholder="Select"
                      style="width: 300px">
                    <el-option
                      v-for="item in userList"
                     
                      :key="item.userId"
                      :label="item.nickName"
                      :value="item.userId">
                      <span style="float: left">{{ item.userId }}</span>
                      <span
                        style="
                          float: right;
                          color: var(--el-text-color-secondary);
                          font-size: 13px;
                        "
                        >{{ item.param.role != null ? item.param.role.name : '未绑定角色' }}</span
                      >
                    </el-option>
                  </el-select>
                </el-form-item>
              
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="assignRole = false">取 消</el-button>
                    <el-button type="primary" @click="saveBind()">保存</el-button>
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
import {getRoleByPage,changeStatus,updateRoleById,deleteRole,createRole,bindRole} from "../api/role";
import {getUserByPage} from "../api/index";

export default {
  name: "role",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      nowId:'',
      roleReq:{},
      search:{},
      page:1,
      total:0,
      pageSize:15,
      isUpdate:false,
      assignRole:false,
      assignData:{},
      userList:[]
    }
  },
  methods:{
    bindUser(row){
      this.assignRole = true
      this.assignData.roleName = row.name
      this.assignData.roleId = row.roleId
      this.assignData.operator = this.adminInfo.userId
      this.assignData.users = []
      for(var i =0 ; i < this.userList.length ;i++){
        if(this.userList[i].roleId == row.roleId){
          this.assignData.users.push(this.userList[i].userId)
        }
      }
    },
    saveBind(){
      bindRole(this.assignData).then(result=>{
        if(result.code === 200) {
          ElMessage.success("绑定成功")
          this.getUserList()
          this.getRoleInfo()
          this.assignRole = false
          this.assignData = {}
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })

    },
    getRoleInfo(){
      this.roleReq.page = this.page
      this.roleReq.pageSize = this.pageSize
      getRoleByPage(this.roleReq).then(result=>{
        if(result.code === 200) {
          this.tableData = result.data.records
          this.total = result.data.total
        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    handleEdit(row) {
      this.editData = row
      // console.log(this.editData)
      this.editVisible = true
      this.isUpdate = true
    },
    addRole(){
      this.editData = {}
      this.editVisible = true
    },
    saveRole(){
      this.editData.operator = this.adminInfo.userId
      if(this.isUpdate){
        updateRoleById(this.editData).then(result =>{
          if(result.code === 200) {
            ElMessage({
              type:'success',
              message:'修改成功'
            })
            this.editVisible = false
            this.getRoleInfo()
            this.isUpdate = false
          } else {
            ElMessage({
              type:'error',
              message:`${result.msg}`
            })
          }
        })
      }else{
        createRole(this.editData).then(result =>{
          if(result.code === 200) {
            ElMessage({
              type:'success',
              message:'创建成功'
            })
            this.editVisible = false
            this.getRoleInfo()
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
      this.search.page = this.page
      this.search.pageSize = this.pageSize
      console.log(this.search)
      getRoleByPage(this.search).then(result=>{
        if(result.code === 200) {
          // console.log(result)
          this.tableData = result.data.records
          this.total = result.data.total
          this.search = {}
          ElMessage.success('查询成功！')
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
      this.getRoleInfo()
    },
    beforeChange(roleId,status,row){
      console.log(status)
      var text = '启用'
      if(status == 0){
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
        changeStatus(roleId,status,this.adminInfo.userId).then((res)=>{
          if(res.code == 200){
            ElMessage.success(text + '成功')
            this.getRoleInfo()
          }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
        if(status == 1){
          row.status = 0
        }else{
          row.status = 1
        }
      })
    },
    handleDelete(roleId){
      var deleteRoleReq = {}
      deleteRoleReq.roleId = roleId
      deleteRoleReq.isAdmin = this.adminInfo.isAdmin
      deleteRoleReq.operator = this.adminInfo.userId

      ElMessageBox.confirm(
        '你确定要删除该角色吗？',
        'Warning',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        deleteRole(deleteRoleReq).then((res)=>{
          if(res.code == 200){
            ElMessage.success('删除成功')
            this.getRoleInfo()
          }else{
            ElMessage.error('删除失败')
          }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
      })
    },
    getUserList(){
      var userReq = {}
      userReq.appId = this.adminInfo.appId
      getUserByPage(userReq).then(result=>{
        if(result.code === 200) {
          this.userList = result.data
          console.log(this.userList)

        } else {
          ElMessage.error(`${result.msg}`)
        }
      })
    },
    

  },
  created() {
    this.getRoleInfo()
    this.getUserList()
  }

};
</script>

<style scoped>
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
</style>
