<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="mgb20" style="height:252px;">
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
            <span style="color:#8a98c9;font-size: 16px;">{{adminInfo.userId}}</span>
          </div>
          <div class="user-info-list">
            我的注册时间
            <span style="color:#8a98c9;font-size: 16px;">{{adminInfo.createTime}}</span>
          </div>
        </el-card>
        <el-card shadow="hover" >
          <template #header>
            <div class="clearfix">
              <span>用户详情</span>
            </div>
          </template>
          <div v-for="(item,index) in userData" :key="index" style="margin-bottom:6px">
              {{item.role}}
            <el-progress :percentage="item.percentage" color="#8a98c9"></el-progress>
          </div>
        
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-row :gutter="20" class="mgb20">
          <el-col :span="8" @click="getUserData">
            <el-card shadow="hover" :body-style="{ padding: '0px' }">
              <div class="grid-content grid-con-1" >
                <i class="el-icon-user-solid grid-con-icon"></i>
                <div class="grid-cont-right" >
                  <div class="grid-num">{{allData.userCount}}</div>
                  <div>IM Chat 用户量</div>
                </div>
                <!-- <div v-else class="grid-cont-right">
                  v-if="adminInfo.isAdmin != 0"
                  <div class="grid-num">{{allData.friendCount}}</div>
                  <div>好友数</div>
                </div> -->
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{ padding: '0px' }">
              <div class="grid-content grid-con-2" >
                <i class="el-icon-success grid-con-icon"></i>
                <div class="grid-cont-right" >
                  <div class="grid-num">{{allData.onlineUser}}</div>
                  <div>在线用户数</div>
                </div>
                <!-- <div v-else class="grid-cont-right">
                  v-if="adminInfo.isAdmin != 0"
                  <div class="grid-num">{{allData.onlineFriend}}</div>
                  <div>在线好友数</div>
                </div> -->
              </div>
            </el-card>
          </el-col>
          <el-col :span="8" @click="getMessageData">
            <el-card shadow="hover" :body-style="{ padding: '0px' }">
              <div class="grid-content grid-con-3">
                <i class="el-icon-message-solid grid-con-icon"></i>
                <div class="grid-cont-right">
                  <div class="grid-num">{{allData.msgCount}}</div>
                  <div>聊天消息总条数</div>
                </div>
              </div>
            </el-card>
          </el-col>

        </el-row>
        <el-card shadow="hover" >
          <!-- <template #header>
            <div class="clearfix">
              <span>待办事项</span>
              <el-button style="float: right; padding: 3px 0" type="text">添加</el-button>
            </div>
          </template> -->

          <!-- <el-table :show-header="false" :data="todoList" style="width:100%;">
            <el-table-column width="40">
              <template #default="scope">
                <el-checkbox v-model="scope.row.status"></el-checkbox>
              </template>
            </el-table-column>
            <el-table-column>
              <template #default="scope">
                <div class="todo-item"
                     :class="{'todo-item-del': scope.row.status,}"
                >{{ scope.row.title }}
                </div>
              </template>
            </el-table-column>
            <el-table-column width="60">
              <template>
                <i class="el-icon-edit"></i>
                <i class="el-icon-delete"></i>
              </template>
            </el-table-column>
          </el-table> -->

          <div class="echart" id="mychart" :style="myChartStyle"></div>

        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import {getSysInfo,getUserPercentage,getDataOfWeek} from "../api/index";
import {getMessageWeekData} from "../api/message";
import * as echarts from "echarts";

export default {
  name: "dashboard",
  data(){
    return {
      adminInfo:JSON.parse(localStorage.getItem('adminInfo')),
      allData:{},
      userData:[],
      sysDataReq : {},
      myChart: {},
      xData: [], //横坐标
      yData: [], //人数数据
      taskData: [], //任务数据
      myChartStyle: { float: "left", width: "100%", height: "400px" } //图表样式
    }
  },
  methods: {
    initEcharts() {
      const option = {
        xAxis: {
          data: this.xData
        },
        legend: { // 图例
          data: ["exact", "expect"],
          top: "0%"
        },
        yAxis: {},
        series: [
          {
            name: "exact",
            data: this.yData,
            type: "line", // 类型设置为折线图
            label: {
              show: true,
              position: "top",
              textStyle: {
                fontSize: 16
              }
            },
            smooth: true
          },
          {
            name: "expect",
            data: this.taskData,
            type: "line", // 类型设置为折线图
            label: {
              show: true,
              position: "bottom",
              textStyle: {
                fontSize: 16
              }
            },
            smooth: true
          }
        ]
      };
      this.myChart = echarts.init(document.getElementById("mychart"));
      this.myChart.setOption(option);
      //随着屏幕大小调节图表
      window.addEventListener("resize", () => {
        this.myChart.resize();
      });
    },
    getUserData(){
      getDataOfWeek(this.adminInfo.appId).then((res)=>{
        this.xData = res.data.date
        this.yData = res.data.actual
        this.taskData = res.data.expect
        this.initEcharts();

      })
    },
    getMessageData(){
      getMessageWeekData(this.adminInfo.appId).then((res)=>{
        this.xData = res.data.date
        this.yData = res.data.actual
        this.taskData = res.data.expect
        this.initEcharts();

      })
    },
  },
  mounted() {
    console.log(this.adminInfo)
    this.sysDataReq.appId = this.adminInfo.appId
    this.sysDataReq.userId = this.adminInfo.userId
    this.sysDataReq.isAdmin = 1
    getSysInfo(this.sysDataReq).then((result)=>{
      if(result.code === 200) {
        this.allData = result.data
      }
    }),
    getUserPercentage(this.adminInfo.appId).then((result)=>{
      if(result.code === 200) {
        this.userData = result.data
      }
    }),
    this.getUserData()
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
  font-size: 14px;
  line-height: 25px;
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
