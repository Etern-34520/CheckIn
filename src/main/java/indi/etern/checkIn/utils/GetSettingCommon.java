package indi.etern.checkIn.utils;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GetSettingCommon {
    private final String rootName;
    List<String> keys;
    
    public GetSettingCommon(String[] keys, String rootName) {
        this.keys = List.of(keys);
        this.rootName = rootName;
    }
    
    public LinkedHashMap<String, Object> doGet() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        List<String> keysWithRoot = new ArrayList<>(keys.size());
        for (String key : keys) {
            keysWithRoot.add(rootName + "." + key);
        }
        List<SettingItem> settingItems = SettingService.singletonInstance.findAllByKeys(keysWithRoot);
        for (SettingItem settingItem : settingItems) {
            result.put(settingItem.getKey().split("\\.")[1],settingItem.getValue(Object.class));
        }
        return result;
    }
}
