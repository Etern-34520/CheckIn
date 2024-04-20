import {ElNotification} from "element-plus";
import PermissionInfo from "@/auth/PermissionInfo.js";
import {h} from "vue";

function randomUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = (Math.random() * 16) | 0,
            v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

const WebSocketConnector = {
    ws: null,
    firstConnect: true,
    promises: {},
    waitingTasks: [],
    timeOut: 5000,
    connect: function (qq, token) {
        let url = window.location.host;
        const ws = new WebSocket(`ws://${url}/checkIn/api/websocket/${qq}`);
        setTimeout(() => {
            if (ws.readyState !== WebSocket.OPEN) {
                ws.close();
                ElNotification({
                    title: 'Websocket连接超时',
                    message: h('button', {
                        type: 'primary',
                        onClick: () => {
                            WebSocketConnector.connect(qq, token);
                        }
                    }, {
                        default: () => '重新连接'
                    }),
                    position: 'bottom-right',
                    type: 'error',
                    duration: 0
                });
            }
        }, this.timeOut);
        ws.onclose = function () {
            console.log('WebSocket closed');
            ElNotification({
                title: 'Websocket已关闭',
                message: h('button', {
                    type: 'primary',
                    onClick: () => {
                        WebSocketConnector.connect(qq, token);
                    }
                }, {
                    default: () => '重新连接'
                }),
                position: 'bottom-right',
                type: 'warning',
                duration: 0,
                dangerouslyUseHTMLString: true
            });
        }
        ws.onerror = function (error) {
            console.log('WebSocket error', error);
            ElNotification({
                title: 'Websocket连接失败',
                message: '请检查网络连接',
                position: 'bottom-right',
                type: 'error',
                duration: 0
            });
        }
        ws.onopen = function () {
            this.send({
                type: "token",
                token: token
            }).then((message) => {
                PermissionInfo.init(message.permissions);
            });
            if (this.waitingTasks.length > 0) {
                this.waitingTasks.forEach((promise) => {
                    let promise1 = this.send(promise["message"], promise["expectResponse"]);
                    promise1.then((message) => {
                        promise.resolve(message);
                    });
                });
                this.waitingTasks = [];
            }
            console.log('WebSocket opened');
        }.bind(this);
        ws.onmessage = function (event) {
            const message = JSON.parse(event.data);
            if (message.messageId) {
                const promise = this.promises[message.messageId];
                if (promise && promise instanceof Promise) {
                    if (message.type === "error") {
                        promise.reject(message);
                        console.error("websocket error", message);
                        ElNotification({
                            title: 'Websocket错误',
                            message: JSON.stringify(message),
                            position: 'bottom-right',
                            type: 'error',
                            duration: 0
                        });
                    } else {
                        promise.resolve(message);
                        console.log("websocket response", message);
                    }
                }
            }
        }.bind(this);
        this.ws = ws;
    },
    send: function (/** Object*/objMessage, expectResponse = true) {
        let ableToSend = this.ws !== null && this.ws.readyState === WebSocket.OPEN;
        objMessage["messageId"] = randomUUID();
        let promise = null;
        if (expectResponse) {
            objMessage["expectResponse"] = true;
            const promiseFunctions = {}
            promise = new Promise((resolve, reject) => {
                promiseFunctions["resolve"] = resolve;
                promiseFunctions["reject"] = reject;
            })
            promise["resolve"] = promiseFunctions["resolve"];
            promise["reject"] = promiseFunctions["reject"];
            if (ableToSend)
                this.promises[objMessage["messageId"]] = promise;
        }
        promise["expectResponse"] = expectResponse;
        promise["message"] = objMessage;
        if (ableToSend) {
            this.ws.send(JSON.stringify(objMessage));
        } else {
            this.waitingTasks.push(promise);
        }
        return promise;
    }
}
export default WebSocketConnector