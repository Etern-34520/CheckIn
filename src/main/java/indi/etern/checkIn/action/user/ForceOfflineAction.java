package indi.etern.checkIn.action.user;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.JsonResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Optional;

@Action(name = "forceOffline")
public class ForceOfflineAction extends JsonResultAction {
    long qqNumber;
    @Override
    public String requiredPermissionName() {
        return "force offline";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        WebSocketService.singletonInstance.sendMessage("{\"type\":\"offLine\"}", String.valueOf(qqNumber));
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = (long) dataMap.get("qqNumber");
    }
}
