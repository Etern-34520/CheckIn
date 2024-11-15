package indi.etern.checkIn.action.setting;

import java.util.LinkedHashMap;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;

import java.util.Map;
import java.util.Optional;

@Action("saveFacadeSetting")
public class SaveFacadeSetting extends TransactionalAction {
    public static final String[] KEYS = {"title", "subTitle", "description", "icon"};
    SaveSettingCommon saveSettingCommon;
    @Override
    public String requiredPermissionName() {
        return "save setting facadeSetting";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        saveSettingCommon.doSave();
        return Optional.ofNullable(getSuccessMap());
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        saveSettingCommon = new SaveSettingCommon((Map<String, Object>) dataMap.get("data"),
                KEYS,
                "facade");
    }
}
