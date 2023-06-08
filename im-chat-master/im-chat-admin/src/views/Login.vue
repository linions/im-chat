<template>
  <div class="login-wrap">
    <div class="ms-login">
      <div class="ms-title">IM Chat 后台管理系统</div>
      <el-form :model="param" :rules="rules" ref="login" label-width="0px" class="ms-content">
        <el-form-item prop="username">
          <el-input v-model="param.userId" placeholder="userId">
            <template #prepend>
              <el-button icon="el-icon-user"></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password" placeholder="password" v-model="param.password"
                    @keyup.enter="submitForm()">
            <template #prepend>
              <el-button icon="el-icon-lock"></el-button>
            </template>
          </el-input>
        </el-form-item>
        <div class="login-btn">
          <el-button type="primary" @click="submitForm()">登录</el-button>
        </div>
        <p class="login-tips"></p>
      </el-form>
    </div>
  </div>
</template>

<script>
import {ref, reactive} from "vue";
import {useStore} from "vuex";
import {useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import {adminLogin,getInfo} from "../api/index";

export default {
  setup() {
    const router = useRouter();
    const param = reactive({
      userId: "admin",
      password: "123456",
    });

    const rules = {
      userId: [
        {
          required: true,
          message: "请输入账号",
          trigger: "blur",
        },
      ],
      password: [
        {required: true, message: "请输入密码", trigger: "blur"},
      ],
    };
    const login = ref(null);
    

    const submitForm = () => {
      login.value.validate((valid) => {
        if (valid) {
          console.log(param)
          adminLogin(param).then(res=>{
            if(res.code === 200) {
              ElMessage.success("登录成功");
              localStorage.setItem("adminToken", res.data);
              localStorage.setItem("adminInfo",JSON.stringify(res.data))
              router.push("/");

              
            } else {
              ElMessage.error(`${res.msg}`);
            }
          })
        } else {
          ElMessage.error("请输入正确的账号和密码");
          return false;
        }
      });
    };

    const store = useStore();
    store.commit("clearTags");

    return {
      param,
      rules,
      login,
      submitForm,
    };
  },
};
</script>

<style scoped>
.login-wrap {
  position: relative;
  width: 100%;
  height: 100%;
  background-image: url(../assets/img/login-bg.jpg);
  background-size: 100%;
}

.ms-title {
  width: 100%;
  line-height: 50px;
  text-align: center;
  font-size: 20px;
  color: #2d2d2d;
  border-bottom: 1px solid #ddd;
}

.ms-login {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 400px;
  margin: -190px 0 0 -175px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  box-shadow: 0 0 3px cadetblue;
}

.ms-content {
  padding: 30px 30px;
}

.login-btn {
  text-align: center;
}

.login-btn button {
  width: 100%;
  height: 36px;
  margin-bottom: 10px;
}

.login-tips {
  font-size: 12px;
  line-height: 30px;
  color: #fff;
}
</style>