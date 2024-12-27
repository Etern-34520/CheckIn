package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.repositories.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SettingService {
    public static SettingService singletonInstance;
    final SettingRepository settingRepository;
    protected SettingService(SettingRepository settingRepository) {
        singletonInstance = this;
        this.settingRepository = settingRepository;
    }
    
//    public String get(String key) {
//        return settingRepository.findById(key).orElse(SettingItem.EMPTY).getValue();
//    }
    
    /*public String set(String key, String value) {
        Optional<SettingItem> optionalSettingItem = settingRepository.findById(key);
        if (optionalSettingItem.isPresent()) {
            SettingItem settingItem = optionalSettingItem.get();
            settingItem.setValue(value);
            settingRepository.save(settingItem);
        } else {
            settingRepository.save(new SettingItem(key, value));
        }
        return value;
    }*/
    
    public SettingItem getItem(String key) {
        return settingRepository.findById(key).orElseThrow();
    }
    
    public SettingItem getItem(String root,String name) {
        return settingRepository.findById(root+"."+name).orElseThrow();
    }
    
    public List<SettingItem> findAllByKeys(Collection<String> keys) {
        return settingRepository.findAllById(keys);
    }
    
    public Optional<SettingItem> findItem(String key) {
        return settingRepository.findById(key);
    }
    
    public void delete(String key) {
        settingRepository.deleteById(key);
    }
    
    public void delete(SettingItem settingItem) {
        settingRepository.delete(settingItem);
    }
    
    public void save(SettingItem settingItem) {
        settingRepository.save(settingItem);
    }
    
    public void exists(String key) {
        settingRepository.existsById(key);
    }
    
    public List<SettingItem> findAllByKeyLike(String key) {
        return settingRepository.findAllByKeyLike(key);
    }
    
    public void setAll(Map<String, Object> dataMap) {
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            save(new SettingItem(entry.getKey(), entry.getValue().toString(), entry.getValue().getClass()));
        }
    }
    
    public void setAll(Iterable<SettingItem> settingItems) {
        settingRepository.saveAll(settingItems);
    }
}
