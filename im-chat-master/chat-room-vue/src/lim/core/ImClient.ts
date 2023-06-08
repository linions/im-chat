import log from "../log/Logger";
import ByteBuffer from '../codec/ByteBuffer';
import { LoginPack } from '../pack/LoginPack';
import WebToolkit from '../common/WebToolkit';
import { w3cwebsocket, IMessageEvent, ICloseEvent } from 'websocket';
import { RequestBase } from '../model/RequestBase';
import { RequestParams } from '../model/RequestParams';
import HttpApi from './HttpApi';
import Beans from '../common/utils';
import { reverse } from 'dns';
import moadl from '../../plugins/modal'
import { useRouter } from 'vue-router'
const router = useRouter()
import {
    MessageCommand,
    MediaCommand,
    FriendShipCommand,
    GroupCommand,
    SystemCommand,
    UserEventCommand,
    ConversationEventCommand
} from '../common/Command';
import { MessagePack } from '../pack/MessagePack';
import { GroupMessagePack } from '../pack/GroupMessagePack';
import { MessageContent } from '../model/dto/MessageContent';

const loginTimeout = 10 * 1000 // 10 seconds
const heartbeatInterval = 10 * 1000 // seconds
let firstMonitorSocket: boolean = false;// 第一次监听socket

export enum State {
    INIT,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    CLOSEING,
    CLOSED,
}

enum TimeUnit {
    Second = 1000,
    Millisecond = 1,
}

export let sleep = async (second: number, Unit: TimeUnit = TimeUnit.Second): Promise<void> => {
    return new Promise((resolve, _) => {
        setTimeout(() => {
            resolve()
        }, second * Unit)
    })
}

export interface IListener {
    onLogin(userId: string): void; // 登录成功事件
    onSocketConnectEvent(url: string, data: any): void; // socket 连接事件
    onSocketErrorEvent(e: any): void;// 异常回调
    onSocketReConnectEvent(): void;// 重连事件
    onSocketReConnectSuccessEvent(): void;// 重连事件
    onSocketCloseEvent(): void;//连接关闭事件
    onP2PMessage(e: any): void;//收到单聊消息事件
    onTestMessage(e: any): void;//收到消息事件 测试用
    // onOfflineMessage(data):void; // 拉取到离线消息事件
    onVoiceCall(e: any):void; // 语音聊天
    onVideoCall(e: any):void; // 视频聊天
    acceptCall(e: any):void; 
    sendOffer(e: any):void; 
    sendAnswer(e: any):void; 
    rejectCall(e: any):void; 
    cancelCall(e: any):void; 
    timeOutCall(e: any):void; 
    hungUpCall(e: any):void; 
    userOnline(e: any):void;
    onGroupMessage(e: any):void;
    videoAck(e:any):void;
}

export class ImClient {

    url: string = ""
	ip: string = ""
	port: string = ""
    userId!: string
    version: number = 1
    clientType: number = 1
    imei: string = WebToolkit.getDeviceInfo(this.clientType).system;
    listeners: IListener | any = null;
    appId!: number
    userSign!: string;
    imeiLength: number = getLen(this.imei);
    state = State.INIT
    // lastOfflineMessageSequence: number = 0;
    // offlineMessageList: Array<any> = new Array<any>()
    httpUrl: string = "http://127.0.0.1:8080/v1"

    private conn?: w3cwebsocket

    constructor() {

    }

    public getRequestBase(): RequestBase {
        return new RequestBase(this.appId, this.clientType, this.imei);
    }

    public isInit(): boolean {
        return this.state == State.CONNECTED;
    }

    public getRequestParams(): RequestParams {
        return new RequestParams(this.appId, this.userId, this.userSign);
    }

    

    public async init(ip: string, port: string, appId: number, userId: string, clientType:number, userSign: string, listeners: any, callback: (sdk: ImClient) => void) {
        var self = this;
        self.ip = ip;
		self.port = port;
        self.appId = appId;
		self.userId = userId;
		self.clientType = clientType;
        self.listeners = listeners;
        
        this.userSign = userSign
		this.url = "ws://" + ip + ":" + port + "/ws";
        var req = new LoginPack(self.appId, self.userId, self.clientType);
        let { success, err, conn } = await limLogin(self.url, req, self);
        if(err){
            console.log("websocket error: ", err)
            callback(self);
        }else if (success) {
            console.log("im 成功 ")

            //登录成功以后操作
            if (!firstMonitorSocket) {
                firstMonitorSocket = true;
            }
            conn.onerror = (error) => {
                console.info("websocket error: ", error)
                // 加入socket 连接事件
                if (typeof imClient.listeners.onSocketErrorEvent === 'function') {
                    imClient.listeners.onSocketErrorEvent(error);
                }
                //异步方法，自动重连
                this.errorHandler(error, req)
            }

            conn.onclose = (e: ICloseEvent) => {
                log.info("event[onclose] fired")
                if (self.state == State.CLOSEING) {
                    let loginOutPack = imClient.buildMessagePack(0x232b, self.userId);
                    conn.send(loginOutPack.pack(false));
                    this.onclose("logout")
                    return
                }
                // socket断开事件 连接事件
                if (typeof imClient.listeners.onSocketCloseEvent === 'function') {
                    imClient.listeners.onSocketCloseEvent();
                }
                // 异步方法 自动重连
                this.errorHandler(new Error(e.reason), req)
            }

            conn.onmessage = (evt) => {
                var bytebuf = new ByteBuffer(evt.data);
                let byteBuffer = bytebuf.int32().int32().unpack();

                let command = byteBuffer[0];//解析command
                let bodyLen = byteBuffer[1];//解析bodylen
                let unpack = bytebuf.vstring(null, bodyLen).unpack();//解析出字符串
                let msgBody = unpack[2];
                // console.log("sdk收到服务端数据：" + msgBody)

                if (command === MediaCommand.CALL_VOICE) {
                    //语音聊天
                    if (typeof imClient.listeners.onVoiceCall === 'function') {
                        imClient.listeners.onVoiceCall(msgBody);
                    }
                }else if (command === MediaCommand.CALL_VIDEO) {
                    //视频聊天
                    if (typeof imClient.listeners.onVideoCall === 'function') {
                        imClient.listeners.onVideoCall(msgBody);
                    }
                }else if (command === MediaCommand.ACCEPT_CALL) {
                    //同意聊天
                    if (typeof imClient.listeners.acceptCall === 'function') {                        
                        imClient.listeners.acceptCall(msgBody);
                    }
                }else if (command === MediaCommand.TRANSMIT_OFFER) {
                    //发送offer
                    if (typeof imClient.listeners.sendOffer === 'function') {
                        imClient.listeners.sendOffer(msgBody);
                    }
                }else if (command === MediaCommand.TRANSMIT_ANSWER) {
                    //发送answer
                    if (typeof imClient.listeners.sendAnswer === 'function') {
                        imClient.listeners.sendAnswer(msgBody);
                    }
                }else if (command === MediaCommand.HANG_UP) {
                    //挂断聊天
                    if (typeof imClient.listeners.hungUpCall === 'function') {
                        imClient.listeners.hungUpCall(msgBody);
                    }
                }else if (command === MediaCommand.REJECT_CALL) {
                    //拒接聊天
                    if (typeof imClient.listeners.rejectCall === 'function') {
                        imClient.listeners.rejectCall(msgBody);
                    }
                }else if (command === MediaCommand.CANCEL_CALL) {
                    //取消聊天
                    if (typeof imClient.listeners.cancelCall === 'function') {
                        imClient.listeners.cancelCall(msgBody);
                    }
                }else if (command === MediaCommand.TIMEOUT_CALL) {
                    //超时聊天
                    if (typeof imClient.listeners.timeoutCall === 'function') {
                        imClient.listeners.timeoutCall(msgBody);
                    }
                }else if (command === MediaCommand.ACK) {
                    //音视频消息ACK
                    if (typeof imClient.listeners.videoAck === 'function') {
                        imClient.listeners.videoAck(msgBody);
                    }
                }else if (command === MessageCommand.MSG_P2P) {
                    //单聊消息收发
                    if (typeof imClient.listeners.onP2PMessage === 'function') {
                        imClient.listeners.onP2PMessage(msgBody);
                    }
                } else if (command === GroupCommand.MSG_GROUP) {
                    //群聊消息收发
                    if (typeof imClient.listeners.onGroupMessage === 'function') {
                        imClient.listeners.onGroupMessage(msgBody);
                    }
                }else {
                    if (typeof imClient.listeners.onTestMessage === 'function') {
                        imClient.listeners.onTestMessage(msgBody);
                    }
                }
            }
            this.conn = conn;
            // console.log("this.conn = ",this.conn)
            this.state = State.CONNECTED
            //拉取离线消息
            // this.loadOfflineMessage();
            //心跳包
            // this.heartbeatLoop(this.conn);

            if (typeof imClient.listeners.onLogin === 'function') {
                imClient.listeners.onLogin(this.userId);
            }
            callback(self);
        } else {
            log.error(err)
        }

    }

    public buildMessagePack(command: number, messagePack: any) {
        var jsonData = JSON.stringify(messagePack);
        let bodyLen = getLen(jsonData);

        let pack = new ByteBuffer(null, 0);
        pack.int32(command).int32(this.version).int32(this.clientType)
            .int32(0x0)
            .int32(this.appId)
            .int32(this.imeiLength)
            .int32(bodyLen)
            .vstring(this.imei, this.imeiLength)
            .vstring(jsonData, bodyLen);
        return pack;
    }

    // 4. 自动重连
    private async errorHandler(error: Error, req: LoginPack) {
        // 如果是主动断开连接，就没有必要自动重连
        // 比如收到被踢，或者主动调用logout()方法
        if (this.state == State.CLOSED || this.state == State.CLOSEING) {
            return
        }
        this.state = State.RECONNECTING
        if (typeof imClient.listeners.onSocketReConnectEvent === 'function') {
            imClient.listeners.onSocketReConnectEvent();
        }
        // 重连10次
        for (let index = 0; index < 10; index++) {
            await sleep(3)
            try {
                log.info("try to relogin")
                // let { success, err } = await this.login()
                let { success, err, conn } = await limLogin(this.url, req, this);
                if (success) {
                    if (typeof imClient.listeners.onSocketReConnectSuccessEvent === 'function') {
                        imClient.listeners.onSocketReConnectSuccessEvent();
                    }
                    return
                }
                log.info(err)
            } catch (error) {
                log.info(error)
            }
        }
        this.onclose("reconnect timeout")
    }

    // 表示连接中止
    private onclose(reason: string) {
        if (this.state == State.CLOSED) {
            return
        }
        this.state = State.CLOSED

        log.info("connection closed due to " + reason)
        this.conn = undefined
        this.userId = ""

        // 加入socket 关闭事件
        if (typeof imClient.listeners.onSocketErrorEvent === 'function') {
            imClient.listeners.onSocketCloseEvent();
        }
    }

    public getSingleUserInfo(uid: string): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/user/data/getSingleUserInfo", this.getRequestParams(), { userId: uid })
            resolve(resp);
        })
    }

    public async syncGetUserInfo(userId: string[]) {
        let api = new HttpApi(this.httpUrl);
        let resp = api.call("/user/data/getUserInfo", this.getRequestParams(), { userIds: userId })
        return resp;
    }

    public getUserInfo(userId: string[]): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/user/data/getUserInfo", this.getRequestParams(), { userIds: userId })
            resolve(resp);
        })
    }

    public getAllFriend(): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/friendship/getAllFriendShip", this.getRequestParams(), { fromId: this.userId })
            resolve(resp);
        })
    }


    

    // 2、心跳
    private heartbeatLoop(conn) {
        let start = Date.now()
        let loop = () => {
            if (this.state != State.CONNECTED) {
                log.error("heartbeatLoop exited")
                return
            }
            if (Date.now() - start >= heartbeatInterval) {
                log.info(`>>> send ping ;`)
                start = Date.now()
                let pingPack = imClient.buildMessagePack(SystemCommand.PING, {});
                conn.send(pingPack.pack(false));
            }
            setTimeout(loop, 500)
        }
        setTimeout(loop, 500)
    }

    //构建单聊媒体聊天消息对象
    public createP2PMediaMessage(to: string,type:number,key:string,mediaContent:Object) {
        let messagePack = new MessagePack(this.appId);
        messagePack.buildMediaMessagePack(this.userId, to, type,key,mediaContent);
        return messagePack;
    }

    //构建单聊消息对象
    public createP2PTextMessage(to: string, text: string, type:number) {
        let messagePack = new MessagePack(this.appId);
        messagePack.buildTextMessagePack(this.userId, to, text,type);
        return messagePack;
    }

    //构建单聊文件对象
    public createP2PFileMessage(to: string, file: object, type:number) {
        let messagePack = new MessagePack(this.appId);
        messagePack.buildFileMessagePack(this.userId, to, file,type);
        return messagePack;
    }



     //构建群聊聊消息对象
     public createGroupTextMessage(group: string, text: string, type:number) {
        let groupMessagePack = new GroupMessagePack(this.appId);
        groupMessagePack.buildTextMessagePack(this.userId, group, text,type);
        return groupMessagePack;
    }

    //构建群聊文件对象
    public createGroupFileMessage(group: string, file: object, type:number) {
        let groupMessagePack = new GroupMessagePack(this.appId);
        groupMessagePack.buildFileMessagePack(this.userId, group, file,type);
        return groupMessagePack;
    }

    //构建群聊媒体聊天消息对象
    public createGroupMediaMessage(group: string,type:number,key:string,mediaContent:Object) {
        let groupMessagePack = new GroupMessagePack(this.appId);
        groupMessagePack.buildMediaMessagePack(this.userId, group, type,key,mediaContent);
        return groupMessagePack;
    }

    

    public sendP2PFileMessage(to: string, text: object,type:number) {
        let pack = this.createP2PFileMessage(to,text,type)
        let p2pPack = imClient.buildMessagePack(MessageCommand.MSG_P2P, pack);

        console.log("pack = ",p2pPack)
        if (this.conn) {
            this.conn.send(p2pPack.pack(false));
        }
    }

    public sendGroupFileMessage(group: string, text: object,type:number) {
        let pack = this.createGroupFileMessage(group,text,type)
        let groupPack = imClient.buildMessagePack(GroupCommand.MSG_GROUP, pack);

        console.log("pack = ",groupPack)
        if (this.conn) {
            this.conn.send(groupPack.pack(false));
        }
    }

    public sendP2PMessage(to: string, text: string,type:number) {
        let pack = this.createP2PTextMessage(to,text,type)
        let p2pPack = imClient.buildMessagePack(MessageCommand.MSG_P2P, pack);

        // console.log("pack = ",p2pPack)
        if (this.conn) {
            this.conn.send(p2pPack.pack(false));
        }
    }

    public sendGroupMessage(group: string, text: string,type:number) {
        let pack = this.createGroupTextMessage(group,text,type)
        let groupPack = imClient.buildMessagePack(GroupCommand.MSG_GROUP, pack);

        console.log("pack = ",groupPack)
        if (this.conn) {
            this.conn.send(groupPack.pack(false));
        }
    }

    public sendP2PVideo(to: string, type:number,key:string,mediaContent:Object) {
        let pack = this.createP2PMediaMessage(to,type,key,mediaContent)
        let p2pPack = imClient.buildMessagePack(MediaCommand.CALL_VOICE, pack);
        if(type == 1){
            // 发送语音
            p2pPack = imClient.buildMessagePack(MediaCommand.CALL_VOICE, pack);
        }else if(type == 2){
            // 发送视频
            p2pPack = imClient.buildMessagePack(MediaCommand.CALL_VIDEO, pack);
        }else if(type == 3){
            // 接收
            p2pPack = imClient.buildMessagePack(MediaCommand.ACCEPT_CALL, pack);
        }else if(type == 4){
            // 拒绝
            p2pPack = imClient.buildMessagePack(MediaCommand.REJECT_CALL, pack);
        }else if(type == 5){
            // 挂断
            p2pPack = imClient.buildMessagePack(MediaCommand.HANG_UP, pack);
        }else if(type == 6){
            // 取消
            p2pPack = imClient.buildMessagePack(MediaCommand.CANCEL_CALL, pack);
        }else if(type == 7){
            // 超时
            p2pPack = imClient.buildMessagePack(MediaCommand.TIMEOUT_CALL, pack);
        }else if(type == 8){
            // 发送offer
            p2pPack = imClient.buildMessagePack(MediaCommand.TRANSMIT_OFFER, pack);
        }else if(type == 9){
            // 发送answer
            p2pPack = imClient.buildMessagePack(MediaCommand.TRANSMIT_ANSWER, pack);
        }
        if (this.conn) {
            this.conn.send(p2pPack.pack(false));
        }
    }

    public getUserId() {
        return this.userId;
    }

    // private async loadOfflineMessage() {
    //     log.info("loadOfflineMessage start")
    //     let api = new HttpApi(this.httpUrl);
    //     let resp = await api.call("/message/syncOfflineMessage",this.getRequestParams(),{clientType : this.clientType,appId : this.appId,lastSequence:this.lastOfflineMessageSequence,maxLimit:100})
    //     if(resp.isSucceed()){
    //         this.lastOfflineMessageSequence = resp.data.maxSequence;
    //         let offmessages = resp.data.dataList;
    //         this.offlineMessageList.push(offmessages)
    //         if(offmessages.length > 0 && typeof imClient.listeners.onOfflineMessage === 'function'){
    //             imClient.listeners.onOfflineMessage(offmessages);
    //         }
    //         console.log(resp.data.completed)
    //         if(!resp.data.completed){
    //             this.loadOfflineMessage();
    //         }
    //     }else{
    //         log.error("loadOfflineMessage - error")
    //     }
    // }

}

export let limLogin = async (url: string, req: LoginPack, imClient: ImClient): Promise<{ success: boolean, err?: Error, conn: w3cwebsocket }> => {
    return new Promise((resolve, _) => {
        let conn = new w3cwebsocket(url)
        conn.binaryType = "arraybuffer"
        // console.info("limLogin url = ",url);
        // console.info("limLogin conn = ",conn);
        
        // 设置一个登陆超时器
        let tr = setTimeout(() => {
            clearTimeout(tr)
            resolve({ success: false, err: new Error("timeout"), conn: conn });
        }, loginTimeout);

        conn.onopen = () => {
            if (conn.readyState == w3cwebsocket.OPEN) {

                // 加入socket 连接事件
                if (typeof imClient.listeners.onSocketConnectEvent === 'function') {
                    imClient.listeners.onSocketConnectEvent(url, req);
                }
                console.info(`开启连接`);
                //登录数据包
                var data = {
                    "userId": req.userId
                }
                let loginPack = imClient.buildMessagePack(0x2328, data);
                conn.send(loginPack.pack(false));
            }
        }
        conn.onerror = (error: Error) => {
            // clearTimeout(tr)
            console.error(error)
            resolve({ success: false, err: error, conn: conn });
        }

        conn.onmessage = (evt) => {
            if (typeof evt.data === 'string') {
                console.info("Received: '" + evt.data + "'");
                return
            }
            // clearTimeout(tr)

            var bytebuf = new ByteBuffer(evt.data);

            let byteBuffer = bytebuf.int32().int32().unpack();

            let command = byteBuffer[0];
            let bodyLen = byteBuffer[1];
            if (command == 0x2329) {
                resolve({ success: true, conn: conn });
            }

        }

    })


}



export let getLen = (str) => {
    var len = 0;
    for (var i = 0; i < str.length; i++) {
        var c = str.charCodeAt(i);
        //单字节加1
        if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
            len++;
        } else {
            len += 3;
        }
    }
    return len;
}




export const imClient = new ImClient();


