package indi.etern.checkIn.action.user;

import java.util.LinkedHashMap;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Map;
import java.util.Optional;

@Action("forceOffline")
public class ForceOfflineAction extends MapResultAction {
    long qqNumber;
    @Override
    public String requiredPermissionName() {
        return "force offline";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        WebSocketService.singletonInstance.sendMessage("{\"type\":\"offLine\"}", String.valueOf(qqNumber));
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        qqNumber = (long) dataMap.get("qqNumber");
    }
}
