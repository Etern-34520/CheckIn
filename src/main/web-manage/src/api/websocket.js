import {ElNotification} from "element-plus";

const WebSocketConnector = {
    ws: null,
    firstConnect: true,
    connect: function (qq) {
        const ws = new WebSocket(`ws://${window.location.host}/api/websocket/${qq}`);
        ws.onclose = function () {
            console.log('WebSocket closed');
            ElNotification({
                title: 'Websocket已关闭',
                message: `
                <button onclick="WebSocketConnector.connect(${qq})">重新连接</button>
                `,
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
            console.log('WebSocket opened');
        }
        ws.onmessage = function (event) {
            console.log(event.data);
        }
        this.ws = ws;
    },
}
export default WebSocketConnector