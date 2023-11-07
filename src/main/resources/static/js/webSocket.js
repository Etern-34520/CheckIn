var websocket = null;

//判断当前浏览器是否支持WebSocket
if ('WebSocket' in window) {
    websocket = new WebSocket("ws://"+window.location.href.split("/manage")[0].replace("http://","")+"/api/websocket/" + $.cookie("qq"));
} else {
    showTip("error","你使用的浏览器不支持WebSocket",false);
}

//连接发生错误的回调方法
websocket.onerror = function (error) {
    showTip("error","WebSocket连接失败"+error,false);
};

//连接成功建立的回调方法
websocket.onopen = function () {
    // showTip("info","WebSocket连接成功");
}

//接收到消息的回调方法
websocket.onmessage = function (event) {
    const message = JSON.parse(event.data);
    switch (message.type) {
        case "updatePartitionList":
            updatePartition(message.partitions);
            break;
        case "error":
            if (message.autoClose === undefined) {
                message.autoClose = true;
            }
            showTip("error",message.data,message.autoClose);
            break;
    }
}

//连接关闭的回调方法
websocket.onclose = function () {
    // setMessageInnerHTML("websocket.onclose: WebSocket连接关闭");
}

//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function () {
    closeWebSocket();
}

//关闭WebSocket连接
function closeWebSocket() {
    websocket.close();
}

//发送消息
function sendMessage(message,func) {
    try {
        websocket.send(message);
        let previousOnMessage = websocket.onmessage;
        websocket.onmessage = function (event) {
            previousOnMessage(event);
            func(event);
            websocket.onmessage = previousOnMessage;
        }
        // setMessageInnerHTML("websocket.send: " + message);
    } catch (err) {
        let data = "websocket.send: " + message + " 失败";
        console.error(data);
        showTip("error",data);
    }
}