import PermissionInfo from "@/auth/PermissionInfo.js";
import {ElNotification} from "element-plus";
import {h} from "vue";
import randomUUIDv4 from "@/utils/UUID.js";
import Reconnect from "@/components/common/Reconnect.vue";
import {jwtDecode} from "jwt-decode";

let qq1;
let token1;

let notifications = {};
let limits = 64 * 1024 / 2 - 1;//64KB

function getCurrentIsoTime() {
    return new Date().toISOString();
}


const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();

function encode(message) {
    return StringToBase64(JSON.stringify(message));
}

function decode(message) {
    return JSON.parse(base64ToString(message));
}

function StringToBase64(bytes) {
    const binString = Array.from(textEncoder.encode(bytes), (byte) =>
        String.fromCodePoint(byte),
    ).join("");
    return btoa(binString);

}
function base64ToString(base64) {
    return textDecoder.decode(Uint8Array.from(atob(base64), c => c.charCodeAt(0)));
}

let autoRetriedTimes = 0;
let normallyClose = false;

const sendInternal = (objMessage) => {
    let message = encode(objMessage);
    if (message.length > limits) {
        const messageParts = [];
        const partCount = Math.ceil(message.length / limits);
        const partMessageIds = [];
        for (let i = 0; i < partCount; i++) {
            const messageId = randomUUIDv4();
            partMessageIds.push(messageId);
            messageParts.push({
                messageId: messageId,
                type: "partMessage",
                data: {
                    partId: objMessage["messageId"],
                    messagePart: message.substring(i * limits, (i + 1) * limits),
                }
            });
        }
        const oldPromise = WebSocketConnector.promises[objMessage["messageId"]];
        const promiseData1 = {};
        let promise1 = new Promise((resolve, reject) => {
            promiseData1["resolve"] = () => {
                for (const message1 of messageParts) {
                    WebSocketConnector.ws.send(encode(message1));
                }
                WebSocketConnector.promises[objMessage["messageId"]] = oldPromise;
            };
            promiseData1["reject"] = reject;
            promiseData1["message"] = objMessage;
        });
        promise1["resolve"] = promiseData1["resolve"];
        promise1["reject"] = promiseData1["reject"];
        promise1["message"] = promiseData1["message"];
        WebSocketConnector.promises[objMessage["messageId"]] = promise1;

        console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] client to server (part message [count: ${partMessageIds.length}]):`, objMessage);

        WebSocketConnector.ws.send(encode({
            messageId: objMessage["messageId"],
            type: "partMessage",
            data: {
                messageIds: partMessageIds,
            }
        }));
    } else {
        console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] client to server (simple message):`, objMessage);

        WebSocketConnector.ws.send(message);
    }
}

const onMessageInternal = (message) => {
        if (WebSocketConnector.actions[message.type] instanceof Array) {
            console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] server to client (action: "${message.type}"):`, message);
            for (const callback of WebSocketConnector.actions[message.type]) {
                callback(message);
            }
        } else if (WebSocketConnector.channels[message.channelName]) {
            const channel1 = WebSocketConnector.channels[message.channelName];
            console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] server to client (channel: "${message.channelName}"):`, message, channel1);
            if (channel1 && channel1.actions instanceof Array) {
                channel1.actions.forEach((callback) => {
                    callback(message);
                });
            }
        } else if (message.messageId) {
            const promise = WebSocketConnector.promises[message.messageId];
            if (promise && promise instanceof Promise) {
                if (message.type === "error") {
                    let showNotification = true;
                    message.disableNotification = () => {
                        showNotification = false;
                    }
                    const doNotification = () => {
                        delete message.disableNotification;
                        if (showNotification) {
                            console.error("websocket error", message);
                            ElNotification({
                                title: '执行操作时出错',
                                message: message.data,
                                position: 'bottom-right',
                                type: 'error',
                                duration: 3000
                            });
                        }
                    }
                    promise.catch(doNotification);
                    promise.reject(message);
                } else {
                    console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] server to client (response for: "${message.messageId}"):`, message);
                    promise.resolve(message);
                }
            }
        } else {
            console.warn(`[ ${getCurrentIsoTime()} ][ WebSocket ] server to client (no handler):`, message);
        }
}
const onMessage = (event) => {
    const message = decode(event.data);
    onMessageInternal(message);
}
const WebSocketConnector = {
    ws: null,
    firstConnect: true,
    promises: {},
    waitingTasks: [],
    actions: {},
    channels: {},
    connect: function (qq, token) {
        qq1 = qq;
        token1 = token;
        let url = window.location.host;
        // let url = "localhost:8080";
        return new Promise((resolve, reject) => {
            // window.cookieStore.get("token").then((result) => {
            //     let date = new Date(result.expires);
            const decoded = jwtDecode(token);
            console.log(decoded);
            if (decoded.exp * 1000 < Date.now()) {
                reject("token expired");
            } else {
                let protocol;
                if (window.location.protocol === "https:") {
                    protocol = "wss:"
                } else {
                    protocol = "ws:"
                }
                const ws = new WebSocket(`${protocol}//${url}/checkIn/api/websocket/${qq}`);
                ws.onclose = function () {
                    if (!normallyClose && autoRetriedTimes < 3) {
                        autoRetriedTimes++;
                        setTimeout(() => {
                            WebSocketConnector.reconnect().then(() => {
                                autoRetriedTimes = 0;
                            }, () => {
                                reject();
                            });
                        }, 3000);
                    } else {
                        autoRetriedTimes = 0;
                        if (!normallyClose) {
                            console.info('WebSocket Closed');
                            if (!notifications["closed"])
                                notifications["closed"] = ElNotification({
                                    title: '与服务器的连接已断开',
                                    message: h(Reconnect, {}, {}),
                                    position: 'bottom-right',
                                    type: 'warning',
                                    duration: 0,
                                    showClose: false
                                });
                            reject();
                        } else {
                            console.info('WebSocket Closed Normally');
                            normallyClose = false;
                        }
                    }
                }
                ws.onerror = function (error) {
                    console.error('WebSocket error', error);
                    if (autoRetriedTimes === 3) {
                        if (!notifications["error"])
                            notifications["error"] = ElNotification({
                                title: '连接服务器失败',
                                message: '请检查网络连接',
                                position: 'bottom-right',
                                type: 'error',
                            });
                        reject();
                    }
                }
                ws.onopen = function () {
                    this.send({
                        type: "token",
                        data: {
                            token: token
                        }
                    });
                    this.registerAction("updatePermissions", (message) => {
                        PermissionInfo.init(message.data);
                    })
                    if (this.waitingTasks.length > 0) {
                        this.waitingTasks.forEach((promise) => {
                            sendInternal(promise["message"]);
                        });
                        this.waitingTasks = [];
                    }
                    const channelEntries = Object.entries(this.channels);
                    if (channelEntries.length > 0) {
                        for (const [name, channel] of channelEntries) {
                            const actions = channel.actions;
                            for (const action of actions) {
                                this.subscribe(name, action);
                            }
                        }
                    }
                    for (const key in notifications) {
                        notifications[key].close();
                    }
                    notifications = {};
                    console.info('WebSocket Opened');
                    window.onclose = function () {
                        WebSocketConnector.close();
                    }
                    resolve();
                }.bind(this);
                ws.onmessage = onMessage;
                this.ws = ws;
            }
        });
        // });
    },
    reconnect() {
        return WebSocketConnector.connect(qq1, token1);
    },
    send: function (/** Object*/objMessage) {
        let ableToSend = this.ws !== null && this.ws.readyState === WebSocket.OPEN;
        objMessage.messageId = randomUUIDv4();
        let promise;
        const promiseData = {};
        promise = new Promise((resolve, reject) => {
            promiseData.resolve = resolve;
            promiseData.reject = reject;
            promiseData.message = objMessage;
        });
        promise.resolve = promiseData.resolve;
        promise.reject = promiseData.reject;
        promise.message = promiseData.message;
        this.promises[objMessage.messageId] = promise;
        if (ableToSend) {
            sendInternal(objMessage);
        } else {
            this.waitingTasks.push(promise);
        }
        return promise;
    },
    registerAction: function (type, callback) {
        if (!(WebSocketConnector.actions[type] instanceof Array)) {
            WebSocketConnector.actions[type] = [];
        }
        WebSocketConnector.actions[type].push(callback);
        const index = WebSocketConnector.actions[type].indexOf(callback);
        return {
            unregister: () => {
                WebSocketConnector.actions[type].splice(index, 1);
            }
        };
    },
    close: function () {
        if (this.ws) {
            normallyClose = true;
            this.ws.close();
            this.ws = null;
            for (const [channelName, channel] of Object.entries(WebSocketConnector.channels)) {
                channel.unsubscribe();
            }
        }
    },
    subscribe: function (channelName, callback) {
        console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] subscribe channel (${channelName})`);
        const unsubscribe = () => {
            if (WebSocketConnector.channels[channelName]) {
                WebSocketConnector.channels[channelName].actions = WebSocketConnector.channels[channelName].actions.filter((cb) => cb !== callback);
            }
        };
        WebSocketConnector.send({
            type: "subscribe",
            data: {
                channel: channelName
            }
        }).then((message) => {
            if (!this.channels[channelName]) {
                this.channels[channelName] = {
                    actions: [],
                    unsubscribe: unsubscribe
                };
            }
            if (message.type === "error") {
                console.error("subscribe error", message);
            } else {
                if (!this.channels[channelName].actions.includes(callback)) {
                    this.channels[channelName].actions.push(callback);
                }
            }
        });
        return {name: channelName, unsubscribe: unsubscribe};
    },
}
export default WebSocketConnector;