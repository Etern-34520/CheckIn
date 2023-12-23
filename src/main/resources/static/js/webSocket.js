let websocket = null;
let firstConnect = true;

function connectWebSocket(button) {
    if ('WebSocket' in window) {
        const rootUrl = window.location.href.split("/manage")[0].replace("http://", "").replace("https://", "");
        const url = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${rootUrl}/api/websocket/${$.cookie("qq")}`;
        websocket = new WebSocket(url);

        if (firstConnect) {
            firstConnect = false;
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

connectWebSocket();
let currentErrorCallback;
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
                showTip("error", message.data, message.autoClose);
                // console.error(message);
            }
            break;
        case "updatePartitionList":
            updatePartition(new Map(Object.entries(message.partitionIdNameMap)));
            break;
        case "deleteQuestion":
            removeQuestionDiv(message.questionMD5);
            break;
        case "updateQuestion":
            updateQuestionDiv(message.question);
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
            $.cookie("logoutReason","你已被强制下线",{path: "/checkIn"})
            logout();
            break;
    }
}

const sendToken = function () {
    const token = $.cookie("token");
    sendMessage({
        type: "token",
        token: token,
    });
};
websocket.onopen = sendToken;

websocket.onclose = function () {
    showTip("error", "WebSocket连接已关闭<br><button rounded highlight onclick='connectWebSocket(this)'>尝试重连</button>", false);
}

window.onbeforeunload = function () {
    closeWebSocket();
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