<template>
  <div class="component-upload-image" >
    <el-upload
      multiple
      list-type="picture-card"
      :action="uploadImgUrl"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      name="photo"
      :data="data"
      :on-remove="handleRemove"
      :show-file-list="true"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :disabled="isDisabled"
      :class="{ hide: fileList.length >= limit }">
      <el-icon class="avatar-uploader-icon put"><plus /></el-icon>
    </el-upload>
    
    <!-- 上传提示 -->
    <div class="el-upload__tip" v-if="!showTip">
      请上传
      <template v-if="fileSize">
        大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <template v-if="fileType">
        格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b>
      </template>
      的文件
    </div>

    <el-dialog v-model="dialogVisible" title="预览" width="500px" append-to-body>
      <img :src="dialogImageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
    </el-dialog>
  </div>
</template>

<script setup>
import moadl from '../../plugins/modal'
import { ref,computed,watch } from 'vue';
import {uninstallLogo} from '../../api/group/group'
import useUserStore from '../../store/modules/user'
import useSwitchStore from "../../store/modules/switch";

const switchStore = useSwitchStore()
const userStore = useUserStore()
console.log("userStore = ",userStore)
const props = defineProps({
  modelValue: [String, Object, Array],
  // 图片数量限制
  limit: {
    type: Number,
    default: 1,
  },
  // 大小限制(MB)
  fileSize: {
    type: Number,
    default: 5,
  },
  // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileType: {
    type: Array,
    default: () => ['png', 'jpg', 'jpeg'],
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true,
  },
  isDisabled: {
    type: Boolean,
  },
  // 上传携带的参数
  data: {
    type: Object,
  },
})

// const { proxy } = getCurrentInstance()
const emit = defineEmits()
const number = ref(0)
const uploadList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const uploadImgUrl = ref("/v1/group/uploadLogo/" + switchStore.GroupIntroData.groupId +"/"+ userStore.userInfo.userId) // 上传的服务器地址
const fileList = ref([])
const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize))

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      // 首先将值转为数组
      const list = Array.isArray(val) ? val : props.modelValue.split('\\')
        console.log("list =",list);

      // 然后将数组转为对象数组
      if(fileList.value.length == 0){
        fileList.value.push({ name: list[list.length-1], url: props.modelValue })
          console.log("fileList.value =",fileList.value);
      }
      
    } 
    // else {
    //   fileList.value = []
    //   return []
    // }
  },
  { deep: true, immediate: true },
)
        // console.log(fileList.value.length);


// 删除图片
function handleRemove(file, files) {
  console.log("file = " ,file)
  fileList.value = []
  // uninstallLogo(file.name)
  console.log("fileList.value =",fileList.value);

  // emit('update:modelValue', listToString(fileList.value))
}

// 上传成功回调
function handleUploadSuccess(res) {
  if (res.code != 200) {
    moadl.msgError(`上传失败，原因:${res.msg}!`)
    moadl.closeLoading()
    fileList.value = []
    return
  }else{
    uploadList.value.push({ name: res.data.name ,url: res.data.url })
    if (uploadList.value.length === number.value) {
      fileList.value = uploadList.value
      uploadList.value = []
      number.value = 0
      emit('photo', res.data)
      moadl.msgSuccess("群头像修改成功")
      location.reload()
    }
    console.log("uploadList.value",uploadList.value)
    console.log("fileList.value",fileList.value)
    moadl.closeLoading()
  }
  
}

// 上传前loading加载
function handleBeforeUpload(file) {
  console.log("file = ",file)
  let isImg = false
  if (props.fileType.length) {
    let fileExtension = ''
    if (file.name.lastIndexOf('.') > -1) {
      fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1)
    }
    isImg = props.fileType.some((type) => {
      if (file.type.indexOf(type) > -1) return true
      if (fileExtension && fileExtension.indexOf(type) > -1) return true
      return false
    })
  } else {
    isImg = file.type.indexOf('image') > -1
  }
  if (!isImg) {
    moadl.msgError(`文件格式不正确, 请上传${props.fileType.join('/')}图片格式文件!`)
    return false
  }
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      moadl.msgError(`上传头像图片大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }
  moadl.loading('正在上传图片，请稍候...')
  number.value++
}

// 文件个数超出
function handleExceed() {
  moadl.msgError(`上传文件数量不能超过 ${props.limit} 个!`)
}

// 上传失败
function handleUploadError() {
  moadl.msgError('上传图片失败')
  moadl.closeLoading()
}

// 预览
function handlePictureCardPreview(file) {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

// 对象转成指定字符串分隔
function listToString(list, separator) {
  let strs = ''
  separator = separator || ','
  for (let i in list) {
    if (undefined !== list[i].url && list[i].url.indexOf('blob:') !== 0) {
      strs += list[i].url + separator
    }
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}
</script>

<style>


.put{
  width: 120px;
  height: 120px;
  margin-top: 10px;
  font-size: 1rem;
}

/* // 隐藏picture-card 上传按钮 */
.hide .el-upload--picture-card {
  display: none;
  outline: invert;
}
</style>