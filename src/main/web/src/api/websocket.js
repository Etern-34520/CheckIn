import PermissionInfo from "@/auth/PermissionInfo.js";
import {ElNotification} from "element-plus";
import {h} from "vue";
import {uuidv7} from "uuidv7";
import Reconnect from "@/components/common/Reconnect.vue";
import {jwtDecode} from "jwt-decode";
// import {encode,decode} from "@ygoe/msgpack"

let qq1;
let token1;

let notifications = {};
let limits = 64 * 1024 / 2 - 1;//64KB

function getCurrentIsoTime() {
    return new Date().toISOString();
}

function encodeMessage(message) {
    return JSON.stringify(message, (key, value) => {
        return value === undefined ? null : value;
    });
}

function decodeMessage(message) {
    return JSON.parse(message);
}

let autoRetriedTimes = 0;
let normallyClose = false;
let lastMessageTimestamp = Date.now();

const sendInternal = (objMessage) => {
    let message = encodeMessage(objMessage);
    if (message.length > limits) {
        const messageParts = [];
        const partCount = Math.ceil(message.length / limits);
        const partMessageIds = [];
        const partId = uuidv7();
        for (let i = 0; i < partCount; i++) {
            const messageId = uuidv7();
            partMessageIds.push(messageId);
            messageParts.push({
                messageId: messageId,
                type: "partMessage",
                data: {
                    partId: partId,
                    messagePart: message.substring(i * limits, (i + 1) * limits),
                }
            });
        }
        lastMessageTimestamp = Date.now();
        console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] client to server (part message [count: ${partMessageIds.length}]):`, objMessage);

        WebSocketConnector.send({
            messageId: partId,
            type: "partMessage",
            data: {
                messageIds: partMessageIds,
            }
        }).then((response) => {
            for (const message1 of messageParts) {
                WebSocketConnector.ws.send(encodeMessage(message1));
            }
            const promise = WebSocketConnector.promises[objMessage.messageId];
            if (promise.message.type === "partMessage") {
                onResponseInternal(objMessage.messageId, response);
            }
        });
    } else {
        console.debug(`[ ${getCurrentIsoTime()} ][ WebSocket ] client to server (simple message):`, objMessage);
        WebSocketConnector.ws.send(message);
    }
}

const onResponseInternal = (messageId, message) => {
    if (messageId) {
        const promise = WebSocketConnector.promises[messageId];
        if (promise && promise instanceof Promise) {
            delete WebSocketConnector.promises[messageId];
            if (message.type === "error") {
                let showNotification = true;
                message.disableNotification = () => {
                    showNotification = false;
                }
                const doNotification = () => {
                    delete message.disableNotification;
                    if (WebSocketConnector.showGlobalNotifications && showNotification) {
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
    } else {
        const messageId = message.messageId;
        onResponseInternal(messageId, message);
    }
}
const onMessage = (event) => {
    const raw = event.data;
    const message = decodeMessage(raw);
    onMessageInternal(message);
}

let pingRetryCount = 0;

const ping = () => {
    function handleRetry() {
        if (WebSocketConnector.ws && WebSocketConnector.ws.readyState === WebSocket.OPEN) {
            pingRetryCount++;
            if (pingRetryCount < 3) {
                simplePing().then(() => {
                    handleNext();
                }, () => {
                    handleRetry();
                });
            } else {
                console.warn(`[ ${getCurrentIsoTime()} ][ WebSocket ] ping pong failed`);
                WebSocketConnector.reconnect().then(() => {
                    pingRetryCount = 0;
                }, () => {
                    console.error(`[ ${getCurrentIsoTime()} ][ WebSocket ] reconnect failed`);
                });
            }
        }
    }

    function simplePing() {
        return new Promise((resolve, reject) => {
            const currentTimestamp = Date.now();
            if (lastMessageTimestamp + 10000 < currentTimestamp) {
                if (WebSocketConnector.ws.readyState === WebSocket.OPEN) {
                    WebSocketConnector.send({
                        type: "ping"
                    }, 5000).then((response) => {
                        resolve(response);
                    }, (error) => {
                        reject(error);
                    });
                } else {
                    reject("websocket closed");
                }
            } else {
                setTimeout(() => {
                    simplePing().then((response) => {
                        resolve(response);
                    }, (error) => {
                        reject(error);
                    })
                }, Math.min(currentTimestamp - lastMessageTimestamp, 0));
            }
        });
    }

    function handleNext() {
        pingRetryCount = 0;
        if (WebSocketConnector.ws && WebSocketConnector.ws.readyState === WebSocket.OPEN) {
            setTimeout(() => {
                ping();
            }, 10000);
        }
    }

    if (WebSocketConnector.wa && WebSocketConnector.ws.readyState === WebSocket.OPEN) {
        simplePing().then(() => {
            handleNext();
        }, (error) => {
            handleRetry();
        })
    }
}

function getPromiseState(promise) {
    const t = {};
    return Promise.race([promise, t])
        .then(v => (v === t) ? "pending" : "fulfilled", () => "rejected");
}

const WebSocketConnector = {
    ws: null,
    firstConnect: true,
    promises: {},
    waitingTasks: [],
    actions: {},
    channels: {},
    showGlobalNotifications: false,
    connect: function (qq, token) {
        qq1 = qq;
        token1 = token;
        let url = window.location.host;
        return new Promise((resolve, reject) => {
            const decoded = jwtDecode(token);
            // console.log(decoded);
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
                ws.onclose = function (e) {
                    if (!normallyClose && autoRetriedTimes < 3) {
                        autoRetriedTimes++;
                        console.info(`[ ${getCurrentIsoTime()} ][ WebSocket ] retrying to connect (${autoRetriedTimes})...`);
                        WebSocketConnector.reconnect().then(() => {
                            autoRetriedTimes = 0;
                        }, () => {
                            reject();
                        });
                    } else {
                        autoRetriedTimes = 0;
                        if (!normallyClose) {
                            console.error(`[ ${getCurrentIsoTime()} ][ WebSocket ] Exceptionally closed:`, e);
                            if (WebSocketConnector.showGlobalNotifications
                                && !notifications["closed"])
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
                            console.info(`[ ${getCurrentIsoTime()} ][ WebSocket ] Normally closed`);
                            normallyClose = false;
                        }
                    }
                }
                ws.onerror = function (error) {
                    console.error(`[ ${getCurrentIsoTime()} ][ WebSocket ] error:`, error);
                    /*
                                        if (autoRetriedTimes === 3) {
                                            if (WebSocketConnector.showGlobalNotifications
                                                && !notifications["error"])
                                                notifications["error"] = ElNotification({
                                                    title: '连接服务器失败',
                                                    message: '请检查网络连接',
                                                    position: 'bottom-right',
                                                    type: 'error',
                                                });
                                            reject();
                                        }
                    */
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
                    console.info(`[ ${getCurrentIsoTime()} ][ WebSocket ] Opened`);
                    window.onclose = function () {
                        WebSocketConnector.close();
                    }
                    ping();
                    resolve();
                }.bind(this);
                ws.onmessage = onMessage;
                this.ws = ws;
            }
        });
        // });
    },
    reconnect() {
        WebSocketConnector.ws.close();
        return WebSocketConnector.connect(qq1, token1);
    },
    send: function (/** Object*/objMessage, timeout = 0) {
        let ableToSend = this.ws !== null && this.ws.readyState === WebSocket.OPEN;
        if (!objMessage.messageId) {
            objMessage.messageId = uuidv7();
        }
        const promiseData = {};
        const promise = new Promise((resolve, reject) => {
            promiseData.resolve = resolve;
            promiseData.reject = reject;
            if (timeout > 0) {
                setTimeout(async () => {
                    const status = await getPromiseState(promise);
                    if (status === "pending") {
                        delete promise.reject;
                        delete promise.resolve;
                        delete WebSocketConnector.promises[objMessage.messageId];
                        reject();
                    }
                }, timeout);
            }
        });
        promise.resolve = promiseData.resolve;
        promise.reject = promiseData.reject;
        promise.message = objMessage;
        WebSocketConnector.promises[objMessage.messageId] = promise;
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