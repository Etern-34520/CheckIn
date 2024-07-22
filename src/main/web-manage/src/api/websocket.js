import {ElButton, ElNotification} from "element-plus";
import PermissionInfo from "@/auth/PermissionInfo.js";
import {h} from "vue";
import randomUUID from "@/utils/UUID.js";
import Reconnect from "@/components/common/Reconnect.vue";

let qq1;
let token1;

let notifications = {};
let limits = 4*1024*1024;//4MB

function getCurrentIsoTime() {
    return new Date().toISOString();
}

let autoRetriedTimes = 0;
let normallyClose = false;

const WebSocketConnector = {
    ws: null,
    firstConnect: true,
    promises: {},
    waitingTasks: [],
    actions: {},
    connect: function (qq, token) {
        qq1 = qq;
        token1 = token;
        let url = "localhost:8080"//TODO window.location.host;
        // let url = window.location.host;
        return new Promise((resolve, reject) => {
            const ws = new WebSocket(`ws://${url}/checkIn/api/websocket/${qq}`);
            ws.onclose = function () {
                if (!normallyClose && autoRetriedTimes < 3) {
                    autoRetriedTimes++;
                    WebSocketConnector.reconnect().then(() => {
                        autoRetriedTimes = 0;
                    },() => {
                    });
                } else {
                    autoRetriedTimes = 0;
                    if (!normallyClose) {
                        console.log('WebSocket Closed');
                        if (!notifications["closed"])
                            notifications["closed"] = ElNotification({
                                title: 'Websocket已关闭',
                                message: h(Reconnect, {}, {}),
                                position: 'bottom-right',
                                type: 'warning',
                                duration: 0,
                            });
                        reject();
                    } else {
                        console.log('WebSocket Closed Normally');
                        normallyClose = false;
                    }
                }
            }
            ws.onerror = function (error) {
                console.log('WebSocket error', error);
                if (!notifications["error"])
                    if (autoRetriedTimes===3) {
                        notifications["error"] = ElNotification({
                            title: 'Websocket连接失败',
                            message: '请检查网络连接',
                            position: 'bottom-right',
                            type: 'error',
                            duration: 0
                        });
                    }
                reject();
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
                for (const key in notifications) {
                    notifications[key].close();
                }
                notifications = {};
                console.log('WebSocket Opened');
                resolve();
            }.bind(this);
            ws.onmessage = function (event) {
                const message = JSON.parse(event.data);
                // delete message.messageId;
                if (WebSocketConnector.actions[message.type]) {
                    WebSocketConnector.actions[message.type](message);
                } else if (message.messageId) {
                    const promise = this.promises[message.messageId];
                    if (promise && promise instanceof Promise) {
                        if (message.type === "error") {
                            promise.reject(message);
                            console.error("websocket error", message);
                            ElNotification({
                                title: 'Websocket错误',
                                message: message.message,
                                position: 'bottom-right',
                                type: 'error',
                                duration: 0
                            });
                        } else {
                            promise.resolve(message);
                            console.log("["+ getCurrentIsoTime() + "] WS resp:", message);
                        }
                    }
                }
            }.bind(this);
            this.ws = ws;
        });
    },
    reconnect() {
        return WebSocketConnector.connect(qq1, token1);
    },
    send: function (/** Object*/objMessage, expectResponse = true) {
        let ableToSend = this.ws !== null && this.ws.readyState === WebSocket.OPEN;
        objMessage["messageId"] = randomUUID();
        let promise;
        if (expectResponse) {
            objMessage["expectResponse"] = true;
            const promiseData = {};
            promise = new Promise((resolve, reject) => {
                promiseData["resolve"] = resolve;
                promiseData["reject"] = reject;
                promiseData["message"] = objMessage;
            })
            promise["resolve"] = promiseData["resolve"];
            promise["reject"] = promiseData["reject"];
            promise["message"] = promiseData["message"];
            if (ableToSend) {
                this.promises[objMessage["messageId"]] = promise;
            }
        }
        if (ableToSend) {
            let message = JSON.stringify(objMessage);
            if (message.length > limits) {
                const messageParts = [];
                const partCount = Math.ceil(message.length / limits);
                const partMessageIds = [];
                for (let i = 0; i < partCount; i++) {
                    const messageId = randomUUID();
                    partMessageIds.push(messageId);
                    messageParts.push({
                        messageId: messageId,
                        type: "partMessage",
                        partId: objMessage["messageId"],
                        messagePart: message.substring(i * limits, (i + 1) * limits),
                    });
                }
                const oldPromise = this.promises[objMessage["messageId"]]
                const promiseData1 = {};
                let promise1 = new Promise((resolve, reject) => {
                    promiseData1["resolve"] = () => {
                        for (const message of messageParts) {
                            this.ws.send(JSON.stringify(message));
                        }
                        this.promises[objMessage["messageId"]] = oldPromise;
                    };
                    promiseData1["reject"] = reject;
                    promiseData1["message"] = objMessage;
                });
                promise1["resolve"] = promiseData1["resolve"];
                promise1["reject"] = promiseData1["reject"];
                promise1["message"] = promiseData1["message"];
                this.promises[objMessage["messageId"]] = promise1;
                this.ws.send(JSON.stringify({
                    messageId: objMessage["messageId"],
                    type: "partMessage",
                    messageIds: partMessageIds,
                }));
            } else {
                this.ws.send(message);
            }
        } else {
            this.waitingTasks.push(promise);
        }
        if (expectResponse)
            return promise;
    },
    registerAction: function (type, callback) {
        WebSocketConnector.actions[type] = callback;
    },
    close: function () {
        if (this.ws) {
            normallyClose = true;
            this.ws.close();
            this.ws = null;
        }
    }
}
export default WebSocketConnector