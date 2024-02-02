package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.repositories.SettingRepository;
import org.springframework.stereotype.Service;

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
    public String get(String key) {
        return settingRepository.findById(key).orElse(SettingItem.EMPTY).getValue();
    }
    
    public void set(String key, String value) {
        Optional<SettingItem> optionalSettingItem = settingRepository.findById(key);
        if (optionalSettingItem.isPresent()) {
            SettingItem settingItem = optionalSettingItem.get();
            settingItem.setValue(value);
            settingRepository.save(settingItem);
        } else {
            settingRepository.save(new SettingItem(key, value));
        }
    }
    
    public SettingItem getItem(String key) {
        return settingRepository.findById(key).orElseThrow();
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
            set(entry.getKey(), entry.getValue().toString());
        }
    }
}
