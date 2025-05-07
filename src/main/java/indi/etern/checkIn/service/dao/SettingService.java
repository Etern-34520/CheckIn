package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.repositories.SettingRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@CacheConfig(cacheNames = "setting")
public class SettingService {
    public static SettingService singletonInstance;
    final SettingRepository settingRepository;
    protected SettingService(SettingRepository settingRepository) {
        singletonInstance = this;
        this.settingRepository = settingRepository;
    }
    
    @Cacheable(key = "#name")
    public SettingItem getItem(String root,String name) {
        return settingRepository.findById(root+"."+name).orElseThrow();
    }
    
    public List<SettingItem> findAllByKeys(Collection<String> keys) {
        return settingRepository.findAllById(keys);
    }
    
    @CacheEvict(key = "key")
    public void delete(String key) {
        settingRepository.deleteById(key);
    }
    
    @CacheEvict(key = "#settingItem.key")
    public void delete(SettingItem settingItem) {
        settingRepository.delete(settingItem);
    }
    
    @CacheEvict(allEntries = true)
    public void setAll(Iterable<SettingItem> settingItems) {
        settingRepository.saveAll(settingItems);
    }
}
