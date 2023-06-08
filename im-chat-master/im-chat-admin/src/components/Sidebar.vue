<template>
    <div class="sidebar">
        <el-menu class="sidebar-el-menu" :default-active="onRoutes" :collapse="collapse" background-color="#324157"
            text-color="#bfcbd9" active-text-color="#20a0ff" unique-opened router>
            <template v-for="item in items">
                <template v-if="item.subs">
                    <el-submenu :index="item.index" :key="item.index">
                        <template #title>
                            <i :class="item.icon"></i>
                            <span>{{ item.title }}</span>
                        </template>
                        <template v-for="subItem in item.subs">
                            <el-submenu v-if="subItem.subs" :index="subItem.index" :key="subItem.index">
                                <template #title>
                                    <i :class="subItem.icon"></i>
                                    <span>{{ subItem.title }}</span>
                                </template>
                                <el-menu-item v-for="(threeItem, i) in subItem.subs" :key="i" :index="threeItem.index">
                                    <i :class="subItem.icon"></i>
                                    <span>{{ subItem.title }}</span>
                                </el-menu-item>
                            </el-submenu>
                            <el-menu-item v-else :index="subItem.index" :key="subItem.item">
                                <i :class="subItem.icon"></i>
                                <span>{{ subItem.title }}</span>
                            </el-menu-item>
                        </template>
                    </el-submenu>
                </template>
                <template v-else>
                    <el-menu-item :index="item.index" :key="item.index">
                        <i :class="item.icon"></i>
                        <template #title>{{ item.title }}</template>
                    </el-menu-item>
                </template>
            </template>
        </el-menu>
    </div>
</template>

<script>
import { computed, watch } from "vue";
import { useStore } from "vuex";
import { useRoute } from "vue-router";
export default {
    setup() {
        const items = [
            {
                icon: "el-icon-lx-home",
                index: "/dashboard",
                title: "系统首页",
            },
            {
                icon: "el-icon-lx-cascades",
                index: "/system",
                title: "系统管理",
                subs:[
                {
                    icon: "el-icon-s-custom",
                    index: "/role",
                    title: "用户角色",
                }, 
                {
                    icon: "el-icon-s-operation",
                    index: "/user",
                    title: "用户信息",
                },
                {
                    icon: "el-icon-s-promotion",
                    index: "/message",
                    title: "消息记录",
                },
                {
                    icon: "el-icon-folder-opened",
                    index: "/file",
                    title: "消息文件",
                },
                {
                    icon: "el-icon-message-solid",
                    index: "/notify",
                    title: "通知公告",
                },
                {
                    icon: "el-icon-setting",
                    index: "/params",
                    title: "系统参数",
                },
                {
                    icon: "el-icon-s-order",
                    index: "/log",
                    title: "操作日志",
                }
                ]
            },
            {
                icon: "el-icon-user",
                index: "/account",
                title: "账号中心",
            },

        ];

        const route = useRoute();

        const onRoutes = computed(() => {
            return route.path;
        });

        const store = useStore();
        const collapse = computed(() => store.state.collapse);

        return {
            items,
            onRoutes,
            collapse,
        };
    },
};
</script>

<style scoped>
.sidebar {
    display: block;
    position: absolute;
    left: 0;
    top: 70px;
    bottom: 0;
    overflow-y: scroll;
}
.sidebar::-webkit-scrollbar {
    width: 0;
}
.sidebar-el-menu:not(.el-menu--collapse) {
    width: 250px;
    font-size: 20px;
}
.sidebar > ul {
    height: 100%;
}
</style>
