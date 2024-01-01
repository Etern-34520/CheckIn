package indi.etern.checkIn.api.webSocket.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Optional;

public class ForceOfflineAction extends JsonResultAction {
    final long qqNumber;
    public ForceOfflineAction(long qqNumber) {
        this.qqNumber = qqNumber;
    }
    @Override
    public String requiredPermissionName() {
        return "force offline";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        WebSocketService.singletonInstance.sendMessage("{\"type\":\"offLine\"}", String.valueOf(qqNumber));
        return Optional.empty();
    }
}
