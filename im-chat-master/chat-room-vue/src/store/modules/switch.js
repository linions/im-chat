import { defineStore,storeToRefs} from 'pinia'

const useSwitchStore = defineStore('switch', {
    state: () => {
        return {
            isPersonal: false,
            isFriends: false,
            isGroups: false,
            isFriendIntro: false,
            isGroupIntro: false,
            FriendIntroData: null,
            GroupIntroData: null,
            isChat: false,
            isGroupChat:false,
            notOnlineChat: false,
            isVideo:false,
            isNotification:false,
            isVoice:false,
        }
    },
    actions: {
        
    },
    getters: {

    }
})

export default useSwitchStore;