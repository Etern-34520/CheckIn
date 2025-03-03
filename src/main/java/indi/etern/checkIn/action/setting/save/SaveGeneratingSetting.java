package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.SaveSettingCommon;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("saveGeneratingSetting")
public class SaveGeneratingSetting extends MapResultAction {
    public static final String[] KEYS = {
            "questionAmount",
            "questionScore",
            "partitionRange",
            "specialPartitionLimits",
            "requiredPartitions",
            "showRequiredPartitions",
            "completingPartitions",
            "drawingStrategy",
            "completingStrategy"
    };
    SaveSettingCommon saveSettingCommon;
    
    @Override
    public String requiredPermissionName() {
        return "save generating setting";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        saveSettingCommon.doSave();
        return Optional.of(getSuccessMap());
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        saveSettingCommon = new SaveSettingCommon((Map<String, Object>) dataMap.get("data"),
                KEYS, "generating");
    }
}
