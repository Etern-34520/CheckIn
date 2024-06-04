package indi.etern.checkIn.action.setting;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.SettingService;

import java.util.Map;
import java.util.Optional;

@Action(name = "saveSetting")
public class SaveSettingAction extends TransactionalAction {
    private String type;
    private Map<String, Object> dataMap;

    @Override
    public String requiredPermissionName() {
        return "save setting " + type;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        SettingService.singletonInstance.setAll(dataMap);
        return successOptionalJsonObject;
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        this.type = (String) dataMap.get("settingName");
    }
}
