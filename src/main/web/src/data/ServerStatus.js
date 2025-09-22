import WebSocketConnector from "@/api/websocket.js";

WebSocketConnector.registerAction("updateServiceStatuses", (message) => {
    ServerStatus.generateAvailability = message.data.generateAvailability;
    ServerStatus.submitAvailability = message.data.submitAvailability;
    ServerStatus.loaded = true;
    ServerStatus.error = false;
});

const ServerStatus = reactive({
    loading: true,
    loaded: false,
    error: true,
    generateAvailability: {
        /*UNAVAILABLE | MAY_FAIL | FULLY_AVAILABLE*/
        status: "",
        reason: ""
    },
    submitAvailability: {
        /*UNAVAILABLE | MAY_FAIL | FULLY_AVAILABLE*/
        status: "",
        reason: ""
    },
    load: () => {
        ServerStatus.loading = true;
        WebSocketConnector.send({
            type: "getServiceStatuses"
        }).then((res) => {
            ServerStatus.loading = false;
            ServerStatus.loaded = true;
            ServerStatus.error = false;
            ServerStatus.generateAvailability = res.data.serverStatuses.generateAvailability;
            ServerStatus.submitAvailability = res.data.serverStatuses.submitAvailability;
        }, (e) => {
            e.disableNotification();
            ServerStatus.loading = false;
            ServerStatus.loaded = true;
            ServerStatus.error = true;
        });
    }
});

ServerStatus.load();

export default ServerStatus;