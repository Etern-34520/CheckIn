let websocket = null;
let firstConnect = true;

function connectWebSocket(button) {
    if ('WebSocket' in window) {
        websocket = new WebSocket("ws://" + window.location.href.split("/manage")[0].replace("http://", "").replace("https://", "") + "/api/websocket/" + $.cookie("qq"));
        if (firstConnect) {
            firstConnect = false;
        } else {
            websocket.onopen = function () {
                showTip("info", "WebSocket连接成功");
                closeTipOfButton(button);
            }
        }
    } else {
        showTip("error", "你使用的浏览器不支持WebSocket", false);
    }
}

connectWebSocket();
websocket.onerror = function (error) {
    showTip("error", "WebSocket连接失败" + error, false);
};

let currentErrorCallback;

websocket.onmessage = function (event) {
    const message = JSON.parse(event.data);
    switch (message.type) {
        case "updatePartitionList":
            updatePartition(new Map(Object.entries(message.partitionIdNameMap)));
            break;
        case "error":
            if (currentErrorCallback instanceof Function) {
                currentErrorCallback(message);
                currentErrorCallback = undefined;
            } else {
                if (message.autoClose === undefined) {
                    message.autoClose = true;
                }
                showTip("error", message.data, message.autoClose);
                console.error(message);
            }
            break;
        case "deleteQuestion":
            removeQuestionDiv(message.questionMD5);
            break;
        case "updateQuestion":
            updateQuestionDiv(message.question);
            break;
    }
}

websocket.onclose = function () {
    showTip("error", "WebSocket连接已关闭<br><button clickable rounded highlight onclick='connectWebSocket(this)'>尝试重连</button>", false);
}

window.onbeforeunload = function () {
    closeWebSocket();
}

function closeWebSocket() {
    websocket.close();
}

function sendMessage(message, callback, errorCallback) {
    try {
        websocket.send(message);
        let previousOnMessage = websocket.onmessage;
        let previousOnError = websocket.onerror;
        websocket.onmessage = function (event) {
            previousOnMessage(event);
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