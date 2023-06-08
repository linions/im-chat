<template>
    <div class="about">
        <v-header />
        <v-sidebar />
        <div class="content-box" :class="{ 'content-collapse': collapse }">
            <v-tags></v-tags>
            <div class="content">
                <router-view v-slot="{ Component }" :key="$route.fullPath">
                    <transition name="move" mode="out-in">
                        <keep-alive :include="tagsList">
                            <component :is="Component" />
                        </keep-alive>
                    </transition>
                </router-view>
                <!-- <el-backtop target=".content"></el-backtop> -->
            </div>
        </div>
    </div>
</template>
<script>
import { computed } from "vue";
import { useStore } from "vuex";
import vHeader from "../components/Header.vue";
import vSidebar from "../components/Sidebar.vue";
import vTags from "../components/Tags.vue";
export default {
    components: {
        vHeader,
        vSidebar,
        vTags,
    },
    setup() {
        const store = useStore();
        const tagsList = computed(() =>
            store.state.tagsList.map((item) => item.name)
        );
        console.log("tagsList = ",tagsList)
        const collapse = computed(() => store.state.collapse);
        console.log("collapse = ",collapse)

        return {
            tagsList,
            collapse,
        };
    },
    // //Vue中写在mounted里
    // mounted() {
    //     window.onbeforeunload= (e)=>{
	// 		 e = e || window.event;			 
	// 		 if (e) {
	// 		 e.returnValue = '关闭提示';
	// 		 }
	// 		//  this.toCloseFun()//调用自己的方法
	// 		 // Chrome, Safari, Firefox 4+, Opera 12+ , IE 9+
	// 		 return '关闭提示';
	// 		};
	// },
};
</script>
