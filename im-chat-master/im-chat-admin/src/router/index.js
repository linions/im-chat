import {createRouter, createWebHashHistory} from "vue-router";
import Home from "../views/Home.vue";
import system from "../views/system.vue";

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  }, {
    path: "/",
    name: "Home",
    component: Home,
    children: [
      {
        path: "/dashboard",
        name: "dashboard",
        meta: {
          title: '系统首页'
        },
        component: () => import ( /* webpackChunkName: "dashboard" */ "../views/Dashboard.vue")
      }, 
      {
        path: "/system",
        name: "system",
        meta: {
          title: '系统管理'
        },
        component: system,
        children: [
          {
            path: "/role",
            name: "role",
            meta: {
              title: '用户角色'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/role.vue")
          },
          {
            path: "/user",
            name: "user",
            meta: {
              title: '用户信息'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/user.vue")
          },
          {
            path: "/message",
            name: "message",
            meta: {
              title: '消息记录'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/message.vue")
          },
          {
            path: "/file",
            name: "file",
            meta: {
              title: '消息文件'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/file.vue")
          },
          {
            path: "/notify",
            name: "notify",
            meta: {
              title: '通知公告'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/notify.vue")
          },
          {
            path: "/params",
            name: "params",
            meta: {
              title: '系统参数'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/params.vue")
          },
          
          {
            path: "/log",
            name: "log",
            meta: {
              title: '操作日志'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/log.vue")
          },
          {
            path: "/account",
            name: "account",
            meta: {
              title: '账号中心'
            },
            component: () => import ( /* webpackChunkName: "table" */ "../views/account.vue")
          }
        ]
      }
      
    ]
  }, {
    path: "/login",
    name: "Login",
    meta: {
      title: '登录'
    },
    component: () => import ( /* webpackChunkName: "login" */ "../views/Login.vue")
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

// router.beforeEach((to, from, next) => {
//   document.title = `${to.meta.title} | vue-manage-system`;
//   const role = localStorage.getItem('crazyAdminToken');
//   if (!role && to.path !== '/login') {
//     next('/login');
//   } else if (to.meta.permission) {
//     // 如果是管理员权限则可进入，这里只是简单的模拟管理员权限而已
//     role === 'admin'
//         ? next()
//         : next('/403');
//   } else {
//     next();
//   }
// });

export default router;