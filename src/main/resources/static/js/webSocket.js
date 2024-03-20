let websocket = null;
let firstConnect = true;
initWebSocket();
let currentErrorCallback;

function initWebSocket(button) {
    const sendToken = function () {
        const token = $.cookie("token");
        sendMessage({
            type: "token",
            token: token,
        });
    };

    function connectWebSocket(button) {
        if ('WebSocket' in window) {
            const rootUrl = window.location.href.split("/manage")[0].replace("http://", "").replace("https://", "");
            const url = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${rootUrl}/api/websocket/${$.cookie("qq")}`;
            websocket = new WebSocket(url);
            if (firstConnect) {
                firstConnect = false;
                websocket.onopen = sendToken;
            } else {
                websocket.onopen = function () {
                    showTip("info", "WebSocket连接成功");
                    closeTipOfButton(button);
                    sendToken();
                }
            }
        } else {
            showTip("error", "你使用的浏览器不支持WebSocket", false);
        }
    }

    if (button !== undefined) {
        connectWebSocket(button);
    } else {
        connectWebSocket();
    }
    websocket.onerror = function (error) {
        showTip("error", "WebSocket连接失败" + error, false);
    };

    websocket.onmessage = function (event) {
        const message = JSON.parse(event.data);
        switch (message.type) {
            case "error":
                if (currentErrorCallback instanceof Function) {
                    const isContinue = currentErrorCallback(message);
                    if (isContinue === false) break;
                    currentErrorCallback = undefined;
                } else {
                    if (message.autoClose === undefined) {
                        message.autoClose = true;
                    }
                    // console.error(message);
                }
                showTip("error", message.message, message.autoClose);
                break;
            case "updatePartitionList":
                updatePartition(new Map(Object.entries(message.partitionIdNameMap)));
                break;
            case "deleteQuestion":
                removeQuestionDiv(message.questionID);
                break;
            case "updateQuestion":
                updateQuestionDiv(message.question);
                break;
            case "updateQuestions":
                let animation = true;
                if (message.questions.length > 20) {
                    animation = false;
                }
                for (let question of message.questions) {
                    updateQuestionDiv(question, animation);
                }
                break;
            case "deleteUser":
                removeUserDiv(message.QQ);
                break;
            case "updateUser":
                updateUserDiv(message.user);
                break;
            case "addUser":
                addUserDiv(message.user);
                break;
            case "offLine":
                $.cookie("logoutReason", "你已被强制下线", {path: "/checkIn"})
                logout();
                break;
            case "userOnline":
                if (message.qq == $.cookie("qq")) break;
                userOnline(message.qq);
                break;
            case "userOffline":
                if (message.qq == $.cookie("qq")) break;
                else
                    userOffline(message.qq);
                break;
            case "trafficLog":
                updateTrafficLog(message.traffic);
                break;
            case "batchCopy":
                updateBatchCopy(message.questionIds, message.partitionIds);
                break;
            case "batchMove":
                updateBatchMove(message.questionIds, message.partitionIds, message.sourcePartitionId);
                break;
        }
    }

    websocket.onclose = function () {
        showTip("error", "WebSocket连接已关闭<br><button rounded highlight onclick='initWebSocket(this)'>尝试重连</button>", false);
    }

    window.onbeforeunload = function () {
        closeWebSocket();
    }
}

function closeWebSocket() {
    websocket.onclose = undefined;
    websocket.close();
}

function sendMessage(message, callback, errorCallback) {
    try {
        if (message instanceof String) {
            websocket.send(message);
        } else {
            websocket.send(JSON.stringify(message));
        }
        let previousOnMessage = websocket.onmessage;
        let previousOnError = websocket.onerror;
        websocket.onmessage = function (event) {
            previousOnMessage(event);
            if (callback instanceof Function)
                callback(event);
            websocket.onmessage = previousOnMessage;
            websocket.onerror = previousOnError;
        }
        if (errorCallback instanceof Function) {
            currentErrorCallback = errorCallback;
        }
        // setMessageInnerHTML("websocket.send: " + message);
    } catch (err) {
        let data = "websocket.send: " + message + " 失败";
        console.error(data);
        showTip("error", data);
    }
}
