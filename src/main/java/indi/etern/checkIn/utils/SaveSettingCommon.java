package indi.etern.checkIn.utils;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;

import java.util.List;
import java.util.Map;

public class SaveSettingCommon {
    private final String rootName;
    final Map<String, Object> dataMap;
    final List<String> enabledKeys;
    
    public SaveSettingCommon(Map<String, Object> dataMap, String[] enabledKeys, String rootName) {
        this.dataMap = dataMap;
        this.enabledKeys = List.of(enabledKeys);
        this.rootName = rootName;
    }
    
    public void doSave() {
        List<SettingItem> settingItems = dataMap.entrySet().stream()
                .filter(entry -> enabledKeys.contains(entry.getKey()))
                .map(entry -> {
                    final Object value = entry.getValue();
                    return new SettingItem(rootName + "." + entry.getKey(), value, value.getClass());
                })
                .toList();
        SettingService.singletonInstance.setAll(settingItems);
    }
}
