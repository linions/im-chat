import Beans from '../common/utils';

export class MessagePack {
    appId: number;
    messageId: string;
    fromId?: string;
    toId?: string;
    messageRandom?: number;
    messageTime?: Date;
    messageBody?: string;
    FileContent?:object;
    messageKey?: string;
    type: number;
    mediaContent?:Object;


    constructor(appId: number) {
        this.messageId = Beans.uuid();
        this.appId = appId;
        this.messageRandom = this.RangeInteger(0, 10000);
        this.messageTime = new Date();
    }

    RangeInteger(min: number, max: number) {
        const range = max - min
        const value = Math.floor(Math.random() * range) + min
        return value
    }


    buildTextMessagePack(fromId: string, toId: string, text: string,type:number) {
        this.fromId = fromId;
        this.toId = toId;
        this.messageBody = text;
        this.type = type
    }

    buildFileMessagePack(fromId: string, toId: string, text: object,type:number) {
        this.fromId = fromId;
        this.toId = toId;
        this.FileContent = text;
        this.type = type
    }

    buildMediaMessagePack(fromId: string, toId: string, type:number,key:string,mediaContent:Object) {
        this.fromId = fromId;
        this.toId = toId;
        this.type = type;
        this.messageKey = key;
        this.mediaContent = mediaContent
    }

    buildCustomerMessagePack(fromId: string, toId: string, type: number, obj: any) {
        this.fromId = fromId;
        this.toId = toId;
        let body = { type: type, content: obj }
        this.messageBody = Beans.json(body);
    }
}