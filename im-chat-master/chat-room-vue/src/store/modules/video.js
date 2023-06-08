import { defineStore,storeToRefs} from 'pinia'
import { ref } from 'vue'

const videoInfoStore = defineStore('video', {
    state: () => {
        return {
            v1 : ref(),
            v2 : ref(),
            pc : ref(),
        }
    },
    actions: {
        async getV1(){
          return new Promise((resolve,reject) => {
            navigator.mediaDevices.getUserMedia({audio:true,video:true}).then(function(value){
                mediaStreamTrack.value = value;
                this.v1.value.srcObject = value
                console.log("v1 =" ,this.v1)

            }).then(()=>resolve()).catch(()=>reject)
        })
      },
    },
    getters: {

    }
})

export default videoInfoStore;