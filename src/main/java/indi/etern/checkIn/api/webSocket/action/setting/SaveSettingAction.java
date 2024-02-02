package indi.etern.checkIn.api.webSocket.action.setting;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.service.dao.SettingService;

import java.util.Map;
import java.util.Optional;

public class SaveSettingAction extends TransactionalAction {
    private String type;
    private Map<String, Object> dataMap;
    
    public SaveSettingAction(Map<String, Object> dataMap, String type) {
        this.dataMap = dataMap;
        this.type = type;
    }
    
    @Override
    public String requiredPermissionName() {
        return "save setting " + type;
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        SettingService.singletonInstance.setAll(dataMap);
        return Optional.empty();
    }
}
