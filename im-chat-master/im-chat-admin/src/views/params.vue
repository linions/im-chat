<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-lx-cascades"></i> 系统参数
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <el-form :inline="true" :model="search" class="demo-form-inline">
          <el-form-item label="参数名称">
            <el-input v-model="search.name" placeholder="参数名称"></el-input>
          </el-form-item>
          <el-form-item label="内置">
            <el-select v-model="search.isSys" placeholder="是否内置">
              <el-option label="是" value= 1></el-option>
              <el-option label="否" value= 0></el-option>
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
          <el-button type="success" style="margin-bottom: 5px;" plain @click="addParam">新增参数</el-button>
            <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="id" fixed label="编号" width="200" align="center"></el-table-column>
                <el-table-column prop="name" label="参数名称" width="300" align="center"></el-table-column>
                <el-table-column prop="paramKey" label="参数字段" width="400" align="center"></el-table-column>
                <el-table-column prop="paramValue" label="参数值" width="400" align="center"></el-table-column>
                <el-table-column prop="isSys" width="100" label="内置" align="center">
                  <template #default="scope">
                    <!-- @click="beforeChange(scope.row.userId,scope.row.forbiddenFlag,scope.row)" -->
                    <el-switch  v-model="scope.row.isSys " :active-value="1" :inactive-value="0" disabled="true"/>
                    </template>
                </el-table-column>
                <el-table-column prop="remark" width="250" label="备注" align="center"></el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                      <el-button   type="primary" @click="handleEdit(scope.row)" round size="small">编辑</el-button>
                      <el-button   type="danger" @click="handleDelete(scope.row.id)" round size="small">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

    
        <el-dialog title="编辑" v-model="editVisible" width="30%">
            <el-form label-width="100px">
                <el-form-item label="参数名称：">
                    <el-input v-model="editData.name"></el-input>
                </el-form-item>
                <el-form-item  label="参数字段：">
                  <el-input v-model="editData.paramKey"></el-input>
                </el-form-item>
                <el-form-item  label="参数值：">
                  <el-input v-model="editData.paramValue"></el-input>
                </el-form-item>
                <el-form-item  label="备注：">
                  <el-input type="textarea"
                    :autosize="{ minRows: 4, maxRows: 8}"
                    placeholder="请输入反馈内容"
                    v-model="editData.remark">
                </el-input>
                </el-form-item>
                <el-form-item label="内置：">
                  <el-select v-model="editData.isSys" placeholder="是否内置">
                    <el-option label="是" :value= '1'></el-option>
                    <el-option label="否" :value= '0'></el-option>
                  </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editVisible = false">取 消</el-button>
                    <el-button type="primary" @click="saveParam()">保存</el-button>
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
import {createParam,updateParam,getParamByPage,deleteParam} from "../api/params";


export default {
  name: "user",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      tableData:[],
      editVisible:false,
      editData:{},
      paramsReq:{},
      search:{},
      page:1,
      pageSize:15,
      total:0,
      isUpdate:false,
    }
  },
  methods:{
    addParam(){
      this.editData = {}
      this.editVisible = true
      this.isUpdate = false
    },
    getParamsInfo(){
      this.paramsReq.page = this.page
      this.paramsReq.pageSize = this.pageSize
      getParamByPage(this.paramsReq).then(result=>{
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
      this.editVisible = true
      this.isUpdate = true
    },
    handleDelete(id){
      ElMessageBox.confirm(
        '你确定要删除该参数配置吗？',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        deleteParam(id,this.adminInfo.appId,this.adminInfo.userId).then((res)=>{
          if(res.code == 200){
            ElMessage.success('该参数已删除')
            this.getParamsInfo()
          }
        })
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '操作取消',
        })
       
      })

    },
    // beforeChange(userId,status,row){
    //   console.log(status)
    //   var text = '启用'
    //   if(status == 1){
    //     text = '禁用'
    //   }
      
    //    ElMessageBox.confirm(
    //     '你确定要' + text + '该角色吗？',
    //     'Warning',
    //     {
    //       confirmButtonText: '确认',
    //       cancelButtonText: '取消',
    //       type: 'warning',
    //     }
    //   ).then(() => {
    //     changeStatus(userId,status,this.adminInfo.userId).then((res)=>{
    //       if(res.code == 200){
    //         ElMessage.success(text + '成功')
    //         this.getParamsInfo()
    //       }
    //     })
    //   }).catch(() => {
    //     ElMessage({
    //       type: 'info',
    //       message: '操作取消',
    //     })
    //     if(status == 1){
    //       row.forbiddenFlag = 0
    //     }else{
    //       row.forbiddenFlag = 1
    //     }
    //   })
    // },
    saveParam(){
      this.editData.operator = this.adminInfo.userId
      this.editData.appId = this.adminInfo.appId
      console.log(this.editData)
      if(this.isUpdate){
        updateParam(this.editData).then(result =>{
        if(result.code === 200) {
          ElMessage({
            type:'success',
            message:'修改成功'
          })
          this.editVisible = false
          this.getParamsInfo()
        } else {
          ElMessage({
            type:'error',
            message:`${result.msg}`
          })
        }
      })
      }else{
        createParam(this.editData).then(result =>{
        if(result.code === 200) {
          ElMessage({
            type:'success',
            message:'新增成功'
          })
          this.editVisible = false
          this.getParamsInfo()
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
      getParamByPage(this.search).then(result=>{
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
      this.getParamsInfo()
    },
  },
  mounted() {
    this.getParamsInfo()
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
